package supersymmetry.loaders.recipes;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.common.item.SuSyMetaItems;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.info.MaterialFlags.IS_MAGNETIC;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class SuSyMaterialRecipeHandler {

    public static void init() {
        SusyOrePrefix.catalystBed.addProcessingHandler(PropertyKey.DUST, SuSyMaterialRecipeHandler::processCatalystBed);
        SusyOrePrefix.catalystPellet.addProcessingHandler(PropertyKey.DUST, SuSyMaterialRecipeHandler::processCatalystPellet);
        SusyOrePrefix.sheetedFrame.addProcessingHandler(PropertyKey.DUST, SuSyMaterialRecipeHandler::processSheetedFrame);
    }

    public static void processCatalystBed(OrePrefix catalystBedPrefix, Material mat, DustProperty property) {
        if (mat.hasFlag(SuSyMaterialFlags.GENERATE_CATALYST_BED)) {
            ModHandler.addShapedRecipe(String.format("catalyst_bed_%s", mat),
                    OreDictUnifier.get(catalystBedPrefix, mat, 1),
                    " S ", "SCS",  " S ",
                    'S', new UnificationEntry(SusyOrePrefix.catalystPellet, mat),
                    'C', SuSyMetaItems.CATALYST_BED_SUPPORT_GRID);

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(SusyOrePrefix.catalystPellet, mat, 4)
                    .input(SuSyMetaItems.CATALYST_BED_SUPPORT_GRID)
                    .outputs(OreDictUnifier.get(catalystBedPrefix, mat, 1))
                    .EUt(VA[ULV]).duration(64)
                    .buildAndRegister();
        }
    }

    public static void processCatalystPellet(OrePrefix catalystPelletPrefix, Material mat, DustProperty property) {
        if (mat.hasFlag(SuSyMaterialFlags.GENERATE_CATALYST_PELLET)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(dust, mat, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_BOLT)
                    .outputs(OreDictUnifier.get(catalystPelletPrefix, mat, 4))
                    .EUt(VA[ULV]).duration(64)
                    .buildAndRegister();
        }
    }

    public static void processSheetedFrame(OrePrefix sheetedFramePrefix, Material mat, DustProperty property) {
        if (!mat.hasFlag(MaterialFlags.GENERATE_FRAME)) return;

        ModHandler.addShapedRecipe(String.format("%s_sheeted_frame", mat),
                OreDictUnifier.get(sheetedFramePrefix, mat, 12),
                "PFP",
                "PHP",
                "PFP",
                'P', new UnificationEntry(OrePrefix.plate, mat),
                'F', new UnificationEntry(OrePrefix.frameGt, mat),
                'H', ToolItems.HARD_HAMMER);

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, mat, 3)
                .input(OrePrefix.frameGt, mat, 1)
                .output(SusyOrePrefix.sheetedFrame, mat, 6)
                .EUt(7)
                .duration(225) //18.75t/craft = 1 stack/min
                .buildAndRegister();

        int voltageMultiplier;

        //BEGIN CLONED MULTIPLIER CODE SECTION
        //CEU COMMENT Gather the highest blast temperature of any material in the list
        int highestTemp = 0;
        if (mat.hasProperty(PropertyKey.BLAST)) {
            BlastProperty prop = mat.getProperty(PropertyKey.BLAST);
            if (prop.getBlastTemperature() > highestTemp) {
                highestTemp = prop.getBlastTemperature();
            }
        }
        else if(mat.hasFlag(IS_MAGNETIC) && mat.hasProperty(PropertyKey.INGOT) && mat.getProperty(PropertyKey.INGOT).getSmeltingInto().hasProperty(PropertyKey.BLAST)) {
            BlastProperty prop = mat.getProperty(PropertyKey.INGOT).getSmeltingInto().getProperty(PropertyKey.BLAST);
            if (prop.getBlastTemperature() > highestTemp) {
                highestTemp = prop.getBlastTemperature();
            }
        }

        //CEU COMMMENT: No blast temperature in the list means no multiplier
        if (highestTemp == 0) voltageMultiplier = 1;

        //CEU COMMENT: If less then 2000K, multiplier of 4
        else if (highestTemp < 2000) voltageMultiplier = 4; // CEU COMMENT: todo make this a better value?

        //CEU COMMENT: If above 2000K, multiplier of 16
        else voltageMultiplier = 16;

        //END CLONED MULTIPLIER CODE SECTION

        //constant int -> output amount int ingots/ multiples of M
        int matRecycleTime = 5 * (int) mat.getMass();

        if (mat.hasProperty(PropertyKey.DUST)) {
            RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .input(SusyOrePrefix.sheetedFrame, mat, 6)
                    .output(dust, mat, 5)
                    .EUt(2 * voltageMultiplier)
                    .duration(matRecycleTime)
                    .buildAndRegister();
        }

        if (mat.hasProperty(PropertyKey.INGOT)) {
            RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                    .input(SusyOrePrefix.sheetedFrame, mat, 6)
                    .output(OrePrefix.ingot, mat, 5)
                    .EUt(VA[LV])
                    .duration(matRecycleTime)
                    .buildAndRegister();
        }

        if (mat.hasFluid()) {
            //L = 144L aka 1 ingot. Considered using M, but I'm pretty sure that isn't meant to be used anywhere other than oredict
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .input(SusyOrePrefix.sheetedFrame, mat, 1)
                    .fluidOutputs(mat.getFluid(120)) // L *5/6
                    .EUt(VA[LV] * voltageMultiplier) //should prevent getting any fluids before you should and fits standard
                    .duration((matRecycleTime /6))
                    .buildAndRegister();
        }

    }
}
