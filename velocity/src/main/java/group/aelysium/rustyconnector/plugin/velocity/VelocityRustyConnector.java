package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.core.lib.exception.DuplicateLifecycleException;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityLifecycle;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.bstats.Metrics;
import group.aelysium.rustyconnector.core.central.PluginRuntime;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.nio.file.Path;

public class VelocityRustyConnector implements PluginRuntime {
    private final Metrics.Factory metricsFactory;
    private static VelocityLifecycle lifecycle;
    private static VelocityAPI api;
    public static VelocityAPI getAPI() {
        return api;
    }
    public static VelocityLifecycle getLifecycle() {
        return lifecycle;
    }

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        api = new VelocityAPI(this, server, logger, dataFolder);
        lifecycle = new VelocityLifecycle();
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) throws DuplicateLifecycleException {
        if(!api.getServer().getConfiguration().isOnlineMode())
            VelocityRustyConnector.getAPI().getLogger().log("Offline mode detected");

        if(!lifecycle.start()) lifecycle.stop();
        try {
            metricsFactory.make(this, 17972);
            VelocityRustyConnector.getAPI().getLogger().log("Registered to bstats!");
        } catch (Exception e) {
            VelocityRustyConnector.getAPI().getLogger().log("Failed to register to bstats!");
        }

        if(!api.getServer().getConfiguration().isOnlineMode())
            VelocityRustyConnector.getAPI().getLogger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text("Your network is running in offline mode! YOU WILL RECEIVE NO SUPPORT AT ALL WITH RUSTYCONNECTOR!"), NamedTextColor.RED));
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {
        try {
            lifecycle.stop();
        } catch (Exception e) {
            VelocityRustyConnector.getAPI().getLogger().log("RustyConnector: " + e.getMessage());
        }
    }
}
