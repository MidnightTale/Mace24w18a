    package net.hynse.mace24w18a;

    import org.bukkit.Bukkit;
    import org.bukkit.Material;
    import org.bukkit.attribute.Attribute;
    import org.bukkit.attribute.AttributeModifier;
    import org.bukkit.enchantments.Enchantment;
    import org.bukkit.entity.Entity;
    import org.bukkit.entity.Player;
    import org.bukkit.event.EventHandler;
    import org.bukkit.event.EventPriority;
    import org.bukkit.event.Listener;
    import org.bukkit.event.entity.EntityDamageByEntityEvent;
    import org.bukkit.event.inventory.CraftItemEvent;
    import org.bukkit.event.inventory.PrepareAnvilEvent;
    import org.bukkit.event.inventory.PrepareItemCraftEvent;
    import org.bukkit.event.player.PlayerMoveEvent;
    import org.bukkit.inventory.EquipmentSlot;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.inventory.meta.ItemMeta;
    import org.bukkit.plugin.java.JavaPlugin;

    import java.util.HashMap;
    import java.util.Map;
    import java.util.UUID;

    public final class Mace24w18a extends JavaPlugin implements Listener {

        private final Map<UUID, Double> fallStartHeights = new HashMap<>();

        @Override
        public void onEnable() {
            Bukkit.getPluginManager().registerEvents(this, this);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            if (fallStartHeights.containsKey(playerUUID) && player.getVelocity().getY() > 0) {
                    fallStartHeights.remove(playerUUID);
                    return;
            }

            if (isFalling(player)) {
                if (!fallStartHeights.containsKey(playerUUID)) {
                    fallStartHeights.put(playerUUID, player.getLocation().getY());
                }
            } else {
                if (fallStartHeights.containsKey(playerUUID)) {
                    fallStartHeights.remove(playerUUID);
                }
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                ItemStack itemInHand = player.getInventory().getItemInMainHand();

                if (isMace(itemInHand)) {
                    UUID playerUUID = player.getUniqueId();
                    if (fallStartHeights.containsKey(playerUUID)) {
                        double startY = fallStartHeights.get(playerUUID);
                        double endY = player.getLocation().getY();
                        double fallDistance = startY - endY;

                        double damage = calculateMaceDamage(fallDistance, itemInHand);
                        event.setDamage(damage);
                    }
                }
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCraftMace(CraftItemEvent event) {
            ItemStack craftedItem = event.getCurrentItem();

            if (craftedItem != null && craftedItem.getType() == Material.MACE) {
                ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.MACE);
                if (meta != null) {
                    meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "Mace Damage", 5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
                    meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "Mace Speed", -3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
                    craftedItem.setItemMeta(meta);
                }
            }
        }

        private boolean isFalling(Player player) {
            return !player.isInsideVehicle() && !player.isInPowderedSnow() && !player.isInBubbleColumn() && !player.isInWater() && !player.isInLava() && !player.isClimbing() && !player.isSleeping() && !player.isDead() && !player.isFlying() && !player.isGliding() && player.getVelocity().getY() < 0 && player.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.AIR;
        }

        private boolean isMace(ItemStack item) {
            return item != null && item.getType() == Material.MACE;
        }

        private double calculateMaceDamage(double fallDistance, ItemStack mace) {
            double damage = 0;
            double step1damage = 3.2;
            double step2damage = 1.6;
            double step3damage = 0.8;
            if (fallDistance > 0) {
                if (fallDistance <= 3) {
                    damage = fallDistance * step1damage;
                } else if (fallDistance <= 8) {
                    damage = 3 * step1damage + (fallDistance - 3) * step2damage;
                } else {
                    damage = 3 * step1damage + 5 * step2damage + (fallDistance - 8) * step3damage;
                }

                int densityLevel = mace.getEnchantmentLevel(Enchantment.DENSITY);
                if (densityLevel > 0 && fallDistance >= 3) {
                    switch (densityLevel) {
                        case 1:
                            damage += 0.5 * fallDistance;
                            break;
                        case 2:
                            damage += 1 * fallDistance;
                            break;
                        case 3:
                            damage += 1.5 * fallDistance;
                            break;
                        case 4:
                            damage += 2 * fallDistance;
                            break;
                        case 5:
                            damage += 2.5 * fallDistance;
                            break;
                        default:
                            break;
                    }
                }


            }
            return damage;
        }
    }