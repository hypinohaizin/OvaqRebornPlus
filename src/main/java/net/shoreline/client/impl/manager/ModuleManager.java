package net.shoreline.client.impl.manager;

import net.shoreline.client.OvaqPlus;
import net.shoreline.client.OvaqMod;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.module.client.*;
import net.shoreline.client.impl.module.combat.*;
import net.shoreline.client.impl.module.exploit.*;
import net.shoreline.client.impl.module.misc.*;
import net.shoreline.client.impl.module.movement.*;
import net.shoreline.client.impl.module.render.*;
import net.shoreline.client.impl.module.world.*;

import java.util.*;

/**
 * @author linus
 * @since 1.0
 */
public class ModuleManager {
    // The client module register. Keeps a list of modules and their ids for
    // easy retrieval by id.
    private final Map<String, Module> modules = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Initializes the module register.
     */
    public ModuleManager() {
        // MAINTAIN ALPHABETICAL ORDER
        register(
                // Client
                new AnticheatModule(),
                new ServerModule(),
                new CapesModule(),
                new ClickGuiModule(),
               // new FontModule(),
                new ColorsModule(),
                new HUDModule(),
                new RPCModule(),
                new IRCModule(),
                //new FontModule(),
                new RotationsModule(),
                new ProtocolModule(),
                // Combat
                //new AntiCrawlModule(),
                new AuraModule(),
                new AutoAnchorModule(),
                new AutoArmorModule(),
                new AutoBowReleaseModule(),
                new AutoObsidianModule(),
                new AutoRegearModule(),
                new AutoCrystalModule(),
                new AutoLogModule(),
                new OffHandModule(),
                new AutoTrapModule(),
                new AutoWebModule(),
                new AutoXPModule(),
                new BlockLagModule(),
                new BowAimModule(),
                new CriticalsModule(),
                new HoleFillModule(),
                new HoleSnapModule(),
                new NoHitDelayModule(),
                new ReplenishModule(),
                new PistonAuraModule(),
                new PistonPushModule(),
                new SelfBowModule(),
                new SelfTrapModule(),
                new SurroundModule(),
                // Exploit
                new AntiHungerModule(),
                new ChorusControlModule(),
                new ChorusInvincibilityModule(),
                new CrasherModule(),
                new DisablerModule(),
                new DosModule(),
                new ExtendedFireworkModule(),
                new FakeLatencyModule(),
                new FastLatencyModule(),
                new FastProjectileModule(),
                new GodModeModule(),
                new HitboxDesyncModule(),
                new NewChunksModule(),
                new NoMineAnimationModule(),
                //new HeartfulExploitModule(),
                new PacketCancelerModule(),
                new PacketControlModule(),
                new PacketFlyModule(),
                new PhaseModule(),
                new PearlPhaseModule(),
                new PortalGodModeModule(),
                new ReachModule(),
                new RegenModule(),
                // Misc
                new AntiAFKModule(),
                new AntiAimModule(),
                new AntiCoordLeakModule(),
                new ChatSuffixModule(),
                new AntiSpamModule(),
                new AntiVanishModule(),
                new AutoAcceptModule(),
                new AutoAnvilRenameModule(),
                new AutoFishModule(),
                new AutoMountModule(),
                new AutoPornModule(),
                new AutoEatModule(),
                new AutoReconnectModule(),
                new AutoRespawnModule(),
                new BeaconSelectorModule(),
                new BetterChatModule(),
                new BetterInvModule(),
                new ChatNotifierModule(),
                new ChestStealerModule(),
                new ChestSwapModule(),
                new DeathCoordModule(),
                new FakePlayerModule(),
                new InvCleanerModule(),
                new MiddleClickModule(),
                new NoPacketKickModule(),
                new NoSoundLagModule(),
                new PacketLoggerModule(),
                new PMSoundModule(),
                new SkinBlinkModule(),
               // new NoTraceModule(),
                new SpammerModule(),
                new TimerModule(),
                new TrueDurabilityModule(),
                new UnfocusedFPSModule(),
                new XCarryModule(),
                // Movement
                new AntiLevitationModule(),
                new AutoWalkModule(),
                new BoatFlyModule(),
                new ElytraFlyModule(),
                new EntityControlModule(),
                new EntitySpeedModule(),
                new FakeLagModule(),
                new FastFallModule(),
                new FastSwimModule(),
                new FireworkBoostModule(),
                new FlightModule(),
                new FollowModule(),
                new IceSpeedModule(),
                new JesusModule(),
                new LongJumpModule(),
                new NoAccelModule(),
                new NoFallModule(),
                new NoJumpDelayModule(),
                new NoSlowModule(),
                new ParkourModule(),
                new SafeWalkModule(),
                new SpeedModule(),
                new SprintModule(),
                new StepModule(),
                new TickShiftModule(),
                new VelocityModule(),
                new YawModule(),
                // Render
                new AmbienceModule(),
                new AnimationsModule(),
                new BlockHighlightModule(),
                new BreadcrumbsModule(),
                new BreakHighlightModule(),
                new ChamsModule(),
                new CrosshairModule(),
                new CrystalModelModule(),
                new ESPModule(),
                new ExtraTabModule(),
                new FreecamModule(),
                new FreeLookModule(),
                new FullbrightModule(),
                new HoleESPModule(),
                new KillEffectsModule(),
                new NameProtectModule(),
                new NametagsModule(),
                new NoBobModule(),
                new NoRenderModule(),
                new NoRotateModule(),
                new NoWeatherModule(),
                new ParticlesModule(),
                new PearlESPModule(),
                new PhaseESPModule(),
                new SkeletonModule(),
                new SkyboxModule(),
                new SmartF3Module(),
                new TooltipsModule(),
                new TracersModule(),
                new TrajectoriesModule(),
                new TrueSightModule(),
                new ViewClipModule(),
                new ViewModelModule(),
                new WorldTimeModule(),
                new ZoomModule(),
                // World
                new AirInteractModule(),
                new AntiInteractModule(),
                new AutoMineModule(),
                new AutoToolModule(),
                new AvoidModule(),
                new FastDropModule(),
                new FastEatModule(),
                new FastPlaceModule(),
                new MultitaskModule(),
                new NoGlitchBlocksModule(),
                new ScaffoldModule(),
                new SpeedmineModule()
                // new WallhackModule()
        );
        if (OvaqMod.isBaritonePresent()) {
            register(new BaritoneModule());
        }
        OvaqPlus.info("Registered {} modules!", modules.size());
    }

    /**
     *
     */
    public void postInit() {
        // TODO
    }

    /**
     * @param modules
     * @see #register(Module)
     */
    private void register(Module... modules) {
        for (Module module : modules) {
            register(module);
        }
    }

    /**
     * @param module
     */
    private void register(Module module) {
        modules.put(module.getId(), module);
    }

    /**
     * @param id
     * @return
     */
    public Module getModule(String id) {
        return modules.get(id);
    }

    /**
     * @return
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }
}
