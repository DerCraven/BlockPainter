package dercraven.blockpainter;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SplittableRandom;

import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class BlockManager implements Listener {

    private boolean playerHoldingPaint;

    Plugin plugin = Bukkit.getPluginManager().getPlugin("BlockPainter");
    FileConfiguration config = plugin.getConfig();
    String paintBrushTool = config.getString("PaintBrushTool");
    //String paintRemoverTool = config.getString("PaintRemoverTool"); WIP

    Integer removeChance = config.getInt("PaintRemoveChance");

    String blockDataString;

    //TO DO:
    //make paint remove tool to turn blocks into their un-painted variants
    //lastly, polish this shit up

    @EventHandler
    public void OnPlayerClicksBlock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Action click = event.getAction();

        String arrayInput = config.getString("PaintableBlocks");
        ArrayList<String> paintableBlocks = new ArrayList<>(Arrays.asList(arrayInput.split(",")));

        //this is here for internal reasons. no you can't remove it
        String dyeList = ("WHITE_DYE,ORANGE_DYE,MAGENTA_DYE,LIGHT_BLUE_DYE,YELLOW_DYE,LIME_DYE,PINK_DYE,GRAY_DYE,LIGHT_GRAY_DYE,CYAN_DYE,PURPLE_DYE,BLUE_DYE,BROWN_DYE,GREEN_DYE,RED_DYE,BLACK_DYE");
        ArrayList<String> Dyes = new ArrayList<>(Arrays.asList(dyeList.split(",")));

        //happens when player clicks on a block that he can edit
        if ((event.getHand() == EquipmentSlot.HAND) && (click == RIGHT_CLICK_BLOCK)) {
            Boolean isPaintable = false; //sorta just re-sets these to avoid problems
            playerHoldingPaint = false;

            String blockname = block.getType().toString(); //the block the player clicked on
            String heldItem = player.getItemInHand().getType().toString(); //the item in players hand
            String offHandItem = player.getInventory().getItemInOffHand().getType().toString(); //item in off-hand
            String origBlockColor = null;

            //check if player has brush in main hand
            if (heldItem.equals(paintBrushTool)) {
                //check if player has dye in off-hand
                if (player.getInventory().getItemInOffHand().getType().toString().contains("DYE")) {
                    for (String substring3 : Dyes) {
                        if (offHandItem.contains(substring3)) {
                            playerHoldingPaint = true;
                            break;
                        }
                    }
                }
            }

            //this bit just checks if the clicked block is part of the PaintableBlocks arrayList
            for(String eee : paintableBlocks) {
                origBlockColor = blockname.replace(("_" + eee), "");
                Boolean validColor = false;
                for (String bbb : Dyes) {
                    if (bbb.replace(("_" + "DYE"), "").equals(origBlockColor)) {
                        validColor = true;
                        break;
                    }
                }
                //player.sendMessage("origBlockColor " + origBlockColor);
                if (blockname.replace((origBlockColor + "_"), "").equals(eee) && validColor) {
                    //player.sendMessage("IT WORKED! " + blockname.replace((origBlockColor + "_"), ""));
                    isPaintable = true;
                    break;
                } //VV this is for edge cases, glass,glass panes, terracotta, unpainted stuff
                else if (eee.equals(blockname)) {
                    //player.sendMessage("this also worked!");
                    isPaintable = true;
                    break;
                }
            }
                //player.sendMessage("isPaintable " + isPaintable);

            if ((isPaintable) && (heldItem.equals(paintBrushTool)) && (playerHoldingPaint)) {
                String colorString = offHandItem.replace("_DYE", ""); // RED_DYE => RED
                //player.sendMessage("block type string " + block.getType());
                //player.sendMessage("colorstring " + colorString);

                for(String subString : paintableBlocks){
                    if(blockname.replace((origBlockColor+"_"),"").equals(subString)){
                        //patch together blockDataString
                        blockDataString = block.getBlockData().getAsString().replace(blockname.toLowerCase(),(colorString + "_" + subString).toLowerCase());
                        break;
                    }
                    else if(blockname.equals("GLASS")){
                        //also patch together blockDataString
                        blockDataString = block.getBlockData().getAsString().replace(blockname.toLowerCase(),(colorString + "_STAINED_GLASS").toLowerCase());
                        break;
                    }
                    else if(blockname.equals("GLASS_PANE")){
                        //also patch together blockDataString
                        blockDataString = block.getBlockData().getAsString().replace(blockname.toLowerCase(),(colorString + "_STAINED_GLASS_PANE").toLowerCase());
                        break;
                    }
                }
                BlockData BD = Bukkit.createBlockData(blockDataString);
                BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(block, block.getState(), block.getRelative(BlockFace.DOWN), player.getInventory().getItemInMainHand(), player, true);
                Bukkit.getServer().getPluginManager().callEvent(blockPlaceEvent);

                if (blockPlaceEvent.isCancelled()) {
                    //WorldGuard sends a message to you here saying "you can't place blocks here"
                } else if (blockname.contains(colorString)) {
                    player.sendMessage(ChatColor.GRAY + "Block is already " + colorString);
                } else {
                    block.setBlockData(BD);

                    //code to remove the paint item
                    SplittableRandom random = new SplittableRandom();
                    boolean teeest = random.nextInt(1, 1001) <= removeChance;
                    if ((teeest) && (player.getGameMode().toString().equals("SURVIVAL"))) {
                        ItemStack offhandItem = player.getInventory().getItemInOffHand();
                        int amount = offhandItem.getAmount();
                        offhandItem.setAmount(amount - 1);
                        player.getInventory().setItemInOffHand(offhandItem);
                    }
                }
            }
        }
    }
}


