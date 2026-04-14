package com.palmergames.bukkit.towny.confirmations;

import com.palmergames.bukkit.towny.object.Translatable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.CancellableTownyEvent;

import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;

/**
 * A class responsible for assembling confirmations.
 */
public class ConfirmationBuilder {
	Runnable acceptHandler;
	Runnable cancelHandler;
	Translatable title = Translatable.of("are_you_sure_you_want_to_continue");
	String confirmCommand = TownySettings.getConfirmCommand();
	String cancelCommand = TownySettings.getCancelCommand();
	String pluginPrefix = "towny";
	int duration = TownySettings.getConfirmationTimeoutSeconds();
	ConfirmationTransaction transaction;
	boolean runAsync;
	CancellableTownyEvent event;
	boolean serious = false;

	/**
	 * The code to run on cancellation.
	 * 
	 * @param cancelHandler The runnable to run on cancellation of the confirmation.
	 * @return A builder reference of this object.
	 */
	public ConfirmationBuilder runOnCancel(Runnable cancelHandler) {
		this.cancelHandler = cancelHandler;
		return this;
	}

	/**
	 * Sets the title of the confirmation to be sent.
	 * 
	 * @param title The title of the confirmation.
	 * @return A builder reference of this object.
	 */
	public ConfirmationBuilder setTitle(String title) {
		this.title = Translatable.literal(title);
		return this;
	}
	
	public ConfirmationBuilder setTitle(Translatable title) {
		this.title = title;
		return this;
	}

	/**
	 * Sets the duration the confirmation will run for. 
	 * 
	 * @param duration The duration in second.
	 * @return A builder reference of this object.
	 */
	public ConfirmationBuilder setDuration(int duration) {
		this.duration = duration;
		return this;
	}

	/**
	 * Builds and sends this confirmation to the given CommandSender.
	 * 
	 * @param confirmationTransaction The ConfirmationTransaction to apply to the confirmation.
	 */
	public ConfirmationBuilder setCost(ConfirmationTransaction confirmationTransaction) {
		this.transaction = confirmationTransaction;
		return this;
	}
	
	public ConfirmationBuilder setCancellableEvent(CancellableTownyEvent event) {
		this.event = event;
		return this;
	}

	/**
	 * Sets whether the confirmation will run it's accept handler
	 * async or not.
	 * 
	 * @param runAsync Whether to run async or not.
	 * @return A builder reference of this object.
	 */
	public ConfirmationBuilder setAsync(boolean runAsync) {
		this.runAsync = runAsync;
		return this;
	}
	
	/**
	 * Sets the command which is displayed/run for the confirm command.
	 * @param confirm String command name.
	 * @return ConfirmationBuilder with an overridden confirm text.
	 */
	public ConfirmationBuilder setConfirmText(String confirm) {
		this.confirmCommand = confirm;
		return this;
	}
	
	/**
	 * Sets the command which is displayed/run for the cancel command.
	 * @param cancel String command name.
	 * @return ConfirmationBuilder with an overriden cancel text.
	 */
	public ConfirmationBuilder setCancelText(String cancel) {
		this.cancelCommand = cancel;
		return this;
	}
	
	/**
	 * Sets the base plugin command used when the cancel or confirm commands are run.
	 * ie: towny in /"towny":town spawn 
	 * @param prefix String plugin name sending this Confirmation.
	 * @return ConfirmationBuilder with an overriden command prefix.
	 */
	public ConfirmationBuilder setPluginCommandPrefix(String prefix) {
		this.pluginPrefix = prefix;
		return this;
	}

	/**
	 * Sets this confirmation as serious, which makes the title be sent in red
	 * @return ConfirmationBuilder with serious marked as true
	 */
	public ConfirmationBuilder serious() {
		this.serious = true;
		return this;
	}
	
	/**
	 * Builds a new instance of {@link Confirmation} from 
	 * this object's state.
	 * 
	 * @return A new Confirmation object.
	 */
	public Confirmation build() {
		return new Confirmation(this);
	}

	/**
	 * Builds and sends this confirmation to the given CommandSender.
	 * 
	 * @param sender The sender to send the confirmation to.
	 */
	public void sendTo(CommandSender sender) {
		if (sender instanceof Player player) {
			// On cible uniquement la création de ville
			if (this.transaction != null && "New Town Cost".equals(this.transaction.getLoggedMessage())) {
				
				Confirmation confirmation = build();
				
				// Appel de notre nouvelle méthode SILENCIEUSE
				ConfirmationHandler.sendConfirmationSilently(player, confirmation);

				// Création de l'inventaire
				Component titleComp = MiniMessage.miniMessage().deserialize("<shift:-8><glyph:towny_confirm_create>");
				Inventory gui = Bukkit.createInventory(null, 54, titleComp);

				if (OraxenItems.exists("empty_slot")) {
					// Création du bouton Confirmer (Vert)
					org.bukkit.inventory.ItemStack confirmBtn = OraxenItems.getItemById("empty_slot").build();
					applyMeta(confirmBtn, "<green>Confirmer", "<gray>Valider la création de la ville");

					// Création du bouton Annuler (Rouge)
					org.bukkit.inventory.ItemStack cancelBtn = OraxenItems.getItemById("empty_slot").build();
					applyMeta(cancelBtn, "<red>Annuler", "<gray>Abandonner");

					// Placement sur les slots demandés
					int[] acceptSlots = {20, 21, 29, 30};
					int[] denySlots = {23, 24, 32, 33};

					for (int s : acceptSlots) gui.setItem(s, confirmBtn);
					for (int s : denySlots) gui.setItem(s, cancelBtn);
				}

				player.openInventory(gui);
				return; 
			}
		}

		// Comportement standard pour les autres messages (chat normal)
		Confirmation confirmation = build();
		ConfirmationHandler.sendConfirmation(sender, confirmation);
	}

	// Utilitaire pour le texte des items
	private void applyMeta(org.bukkit.inventory.ItemStack item, String name, String lore) {
		var meta = item.getItemMeta();
		if (meta == null) return;
		meta.displayName(MiniMessage.miniMessage().deserialize("<!italic>" + name));
		meta.lore(java.util.List.of(MiniMessage.miniMessage().deserialize("<!italic>" + lore)));
		item.setItemMeta(meta);
	}
}
