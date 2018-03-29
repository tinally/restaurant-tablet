package edu.toronto.csc207.restaurantsolution.gui.Server;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import edu.toronto.csc207.restaurantsolution.gui.NetworkContainer;
import edu.toronto.csc207.restaurantsolution.model.implementations.OrderImpl;
import edu.toronto.csc207.restaurantsolution.model.interfaces.Ingredient;
import edu.toronto.csc207.restaurantsolution.model.interfaces.MenuItem;
import edu.toronto.csc207.restaurantsolution.model.interfaces.Order;
import edu.toronto.csc207.restaurantsolution.model.interfaces.OrderStatus;
import edu.toronto.csc207.restaurantsolution.remoting.DataListener;
import edu.toronto.csc207.restaurantsolution.remoting.DataManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;

import java.rmi.RemoteException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controls the Server graphics user interface.
 */
public class ServerController implements DataListener {

  public void confirmSelectedOrder() throws RemoteException {
    TreeItem<DeliverableOrderMapping> selectedItem = deliverableOrdersTable.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Order order = selectedItem.getValue().order;
      manager.modifyOrder(order, OrderStatus.DELIVERED);
    }
  }

  public void rejectSelectedOrder() throws RemoteException {
    TreeItem<DeliverableOrderMapping> selectedItem = deliverableOrdersTable.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Order order = selectedItem.getValue().order;
      manager.modifyOrder(order, OrderStatus.RETURNED);
    }
  }

  public class DeliverableOrderMapping extends RecursiveTreeObject<DeliverableOrderMapping> {
    final IntegerProperty tableNumber;
    final IntegerProperty orderNumber;
    final ObjectProperty<MenuItem> menuItem;
    final Order order;

    DeliverableOrderMapping(Integer tableNumber, Integer orderNumber, MenuItem menuItem, Order order) {
      this.tableNumber = new SimpleIntegerProperty(tableNumber);
      this.orderNumber = new SimpleIntegerProperty(orderNumber);
      this.menuItem = new SimpleObjectProperty<>(menuItem);
      this.order = order;
    }

    public IntegerProperty tableNumberProperty() {
      return tableNumber;
    }

    public IntegerProperty orderNumberProperty() {
      return orderNumber;
    }

    public ObjectProperty<MenuItem> menuItemProperty() {
      return menuItem;
    }
  }

  private final DataManager manager;

  @FXML
  TextArea orderSummaryTextArea;

  public ServerController() throws Exception {
    NetworkContainer.initManager();
    manager = NetworkContainer.dataManager;
    NetworkContainer.dataService.registerListener(this);
  }

  @FXML
  JFXComboBox<Integer> tableNumberSelection;

  @FXML
  JFXListView<Ingredient> deletionsList;

  @FXML
  JFXListView<MenuItem> menuList;

  @FXML
  JFXListView<Ingredient> additionsList;

  @FXML
  JFXTreeTableView<DeliverableOrderMapping> deliverableOrdersTable;

  @Override
  public void update() {
    try {
      ObservableList<MenuItem> menuItems = FXCollections.observableArrayList(manager.getAllMenuItems());
      menuList.setItems(menuItems);

      List<DeliverableOrderMapping> deliverableOrders = manager.getAllOrders().stream()
          .filter(o -> o.getOrderStatus() == OrderStatus.FILLED)
          .map(o -> new DeliverableOrderMapping(o.getTableNumber(), o.getOrderNumber(), o.getMenuItem(), o))
          .collect(Collectors.toList());

      TreeItem<DeliverableOrderMapping> root =
          new RecursiveTreeItem<>(FXCollections.observableArrayList(deliverableOrders),
              RecursiveTreeObject::getChildren);
      this.deliverableOrdersTable.setRoot(root);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendNewOrder() throws RemoteException {
    // TODO: Change to Order / add all to interface
    OrderImpl order = new OrderImpl();

    MenuItem menuItem = menuList.getSelectionModel().getSelectedItem();
    List<Ingredient> additions = additionsList.getSelectionModel().getSelectedItems();

    if (menuItem != null) {
      order.setOrderNumber(new Random().nextInt(1000));
      order.setOrderStatus(OrderStatus.CREATED);
      order.setOrderId(UUID.randomUUID());
      order.setMenuItem(menuList.getSelectionModel().getSelectedItem());
      System.out.println(order.getMenuItem());
      order.setTableNumber(tableNumberSelection.getValue());
      order.setOrderDate(Instant.now());

      HashMap<Ingredient, Integer> additionsMap = new HashMap<>();
      if (additions != null) {
        order.setOrderCost(getOrderCost(menuItem, additions));
        for (Ingredient i : additions) {
          additionsMap.put(i, 1);
        }
      }
      order.setAdditions(additionsMap);
      order.setCreatingUser("system");
      order.setRemovals(new ArrayList<>(this.deletionsList.getSelectionModel().getSelectedItems()));

      manager.modifyOrder(order);
    }
  }

  private Double getOrderCost(MenuItem m, List<Ingredient> additions) {
    double sum = m.getPrice();
    for (Ingredient ingredient : additions)
      sum += ingredient.getPricing();
    return sum;
  }

  private void updateAdditionsAndDeletions(MenuItem item) {
    if (item != null) {
      ObservableList<Ingredient> possibleDeletions = FXCollections
          .observableArrayList(item.getIngredientRequirements().keySet());
      this.deletionsList.setItems(possibleDeletions);
      ObservableList<Ingredient> possibleAdditions = null;
      try {
        possibleAdditions = FXCollections
            .observableArrayList(manager.getAllIngredients());
      } catch (RemoteException e) {
        e.printStackTrace();
      }
      this.additionsList.setItems(possibleAdditions);
    }
  }

  private void updateOrderSummary() {
    StringBuilder orderSummary = new StringBuilder();

    MenuItem selectedItem = menuList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      orderSummary.append(selectedItem.getName()).append(System.lineSeparator());
    }

    for (Ingredient i : this.deletionsList.getSelectionModel().getSelectedItems()) {
      orderSummary.append(" WITHOUT ").append(i.getName()).append(System.lineSeparator());
    }
    for (Ingredient i : this.additionsList.getSelectionModel().getSelectedItems()) {
      orderSummary.append(" EXTRA ").append(i.getName()).append(System.lineSeparator());
    }

    this.orderSummaryTextArea.setText(orderSummary.toString());
  }

  @FXML
  public void initialize() {
    this.deletionsList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
    this.deletionsList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super Ingredient>) e -> {
      this.updateOrderSummary();
    });
    this.additionsList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
    this.additionsList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super Ingredient>) e -> {
      this.updateOrderSummary();
    });
    this.menuList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super MenuItem>) e -> {
      this.deletionsList.getSelectionModel().clearSelection();
      this.additionsList.getSelectionModel().clearSelection();
      this.updateAdditionsAndDeletions(this.menuList.getSelectionModel().getSelectedItem());
      this.updateOrderSummary();
    });
    this.update();
  }
}
