package restaurant;

import kitchen.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An OrderItem represents the order that the customer took.
 */
public class OrderItem {
    /**
     * The MenuItem the customer ordered.
     */
    private MenuItem menuItem;
    /**
     * A HashMap showing how much each Ingredient should be added.
     */
    private HashMap<Ingredient, Integer> addIngredients;
    /**
     * An ArrayList showing the unwanted Ingredients from the MenuItem.
     */
    private ArrayList<Ingredient> removedIngredients;

    public OrderItem(MenuItem menuItem) {
        this(menuItem, new HashMap<>(), new ArrayList<>());
    }

    public OrderItem(MenuItem menuItem, HashMap<Ingredient, Integer> add, ArrayList<Ingredient> removedIngredients) {
        this.menuItem = menuItem;
        this.addIngredients = add;
        this.removedIngredients = removedIngredients;
    }

    /**
     * @return The menuItem that was ordered.
     */
    public MenuItem getMenuItem() {
        return menuItem;
    }

    /**
     * @param menuItem to be set
     */
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    /**
     * @param add Ingredient to be added
     */
    public void addIngredients(HashMap<Ingredient, Integer> add) {
        this.addIngredients = add;
//        this.menuItem.increasePrice(); //TODO: some sort of for loop
        // TODO: Add a retail price for adding an ingredient in Ingredient class.
    }

    /**
     * Add one type of Ingredient at a time
     *
     * @param ingredient the Ingredient to be added
     * @param quantity   the amount of that Ingredient to be added
     */
    public void addIngredient(Ingredient ingredient, int quantity) {
        addIngredients.put(ingredient, quantity);
        // menuItem.increasePrice(); //TODO: Add a retail price for adding an ingredient in Ingredient class.
    }


    /**
     * @param unwantedIngredient Ingredient to be removed
     */
    public void removeIngredient(Ingredient unwantedIngredient) {
        removedIngredients.add(unwantedIngredient);
    }

    /**
     * @return Ingredients to be removed.
     */
    public ArrayList<Ingredient> getRemovedIngredients() {
        return removedIngredients;
    }

    @Override
    public String toString() {
        return "This may be useful"; // TODO: toString
    }
}
