package br.com.lojasquare.qrcode.utils.bukkit;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class GUIAPI {

    private Inventory inv;
    private int tamanho;
    private String nome;

    public GUIAPI(int tamanho, String nome){
        this.tamanho = tamanho;
        this.nome = nome;
        inv = Bukkit.createInventory(null, this.tamanho, this.nome);
    }

    public void completSlotEmpty(ItemStack is){
        for(int i=0; i<getTamanho();i++){
            if(Objects.isNull(inv.getItem(i)) || Material.AIR.equals(inv.getItem(i).getType())){
                inv.setItem(i, is);
            }
        }
    }

    public void addItem(ItemStack ... item){
        inv.addItem(item);
    }

    public void addItemInNextEmpty(ItemStack item){
        inv.setItem(inv.firstEmpty(), item);
    }

    public void setItem(int slot, ItemStack item){
        inv.setItem(slot - 1, item);
    }

    public void setItem(int slot, int linha, ItemStack item){
        inv.setItem(linha * 9 + slot - 1 - 9, item);
    }

    public void openToPlayer(Player p){
        p.closeInventory();
        p.openInventory(inv);
    }

    public void setAllSlots(ItemStack item){
        for (int i = 1; i < tamanho; i++){
            setItem(i, item);
        }
    }

    public Inventory getInventory(){
        return inv;
    }

    public void clear(){
        inv.clear();
    }

    public int getTamanho() {
        return tamanho;
    }

    public String getNome() {
        return nome;
    }

}