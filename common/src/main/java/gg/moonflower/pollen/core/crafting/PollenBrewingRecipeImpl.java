package gg.moonflower.pollen.core.crafting;

import com.google.gson.JsonObject;
import gg.moonflower.pollen.api.crafting.v1.PollenBrewingRecipe;
import gg.moonflower.pollen.api.crafting.v1.PollenRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ocelot
 * @since 1.0.0
 */
public class PollenBrewingRecipeImpl implements PollenBrewingRecipe {

    private final ResourceLocation id;
    private final String group;
    private final Potion from;
    private final Ingredient ingredient;
    private final Potion result;

    public PollenBrewingRecipeImpl(ResourceLocation id, String group, Potion from, Ingredient ingredient, Potion result) {
        this.id = id;
        this.group = group;
        this.from = from;
        this.ingredient = ingredient;
        this.result = result;
    }

    @ApiStatus.Internal
    public static PollenBrewingRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
        String group = GsonHelper.getAsString(jsonObject, "group", "");
        Potion from = BuiltInRegistries.POTION.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "from")));
        Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonObject, "ingredient"));
        Potion result = BuiltInRegistries.POTION.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "result")));
        return new PollenBrewingRecipeImpl(resourceLocation, group, from, ingredient, result);
    }

    @ApiStatus.Internal
    public static PollenBrewingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        String group = buf.readUtf();
        Potion from = BuiltInRegistries.POTION.get(buf.readResourceLocation());
        Ingredient ingredient = Ingredient.fromNetwork(buf);
        Potion result = BuiltInRegistries.POTION.get(buf.readResourceLocation());
        return new PollenBrewingRecipeImpl(id, group, from, ingredient, result);
    }

    @ApiStatus.Internal
    public static void toNetwork(FriendlyByteBuf buf, PollenBrewingRecipe recipe) {
        buf.writeUtf(recipe.getGroup());
        buf.writeResourceLocation(BuiltInRegistries.POTION.getKey(recipe.getFrom()));
        recipe.getIngredient().toNetwork(buf);
        buf.writeResourceLocation(BuiltInRegistries.POTION.getKey(recipe.getResult()));
    }

    public Potion getFrom() {
        return from;
    }

    @Override
    public Ingredient getIngredient() {
        return ingredient;
    }

    @Override
    public Potion getResult() {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public String getGroup() {
        return this.group;
    }
}
