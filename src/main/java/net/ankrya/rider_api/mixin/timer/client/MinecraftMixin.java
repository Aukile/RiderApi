package net.ankrya.rider_api.mixin.timer.client;

import com.mojang.blaze3d.platform.WindowEventHandler;
import net.ankrya.rider_api.data.ModVariable;
import net.ankrya.rider_api.data.Variables;
import net.ankrya.rider_api.help.GJ;
import net.ankrya.rider_api.interfaces.timer.ITimer;
import net.ankrya.rider_api.interfaces.timer.TimerClientLevel;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.IMinecraftExtension;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, IMinecraftExtension {

    @Shadow
    public ClientLevel level;
    @Final
    @Shadow
    private SoundManager soundManager;
    @Shadow
    private volatile boolean pause;
    @Final
    @Shadow
    private DeltaTracker.Timer timer;
    @Shadow
    private ProfilerFiller profiler;
    @Shadow
    @Nullable
    private Overlay overlay;
    @Final
    @Shadow
    public GameRenderer gameRenderer;
    @Final
    @Shadow
    public Options options;
    @Shadow
    public Screen screen;
    @Shadow
    private int rightClickDelay;
    @Shadow
    public MultiPlayerGameMode gameMode;
    @Shadow
    public LocalPlayer player;
    @Final
    @Shadow
    private MusicManager musicManager;
    @Shadow
    private TutorialToast socialInteractionsToast;
    @Final
    @Shadow
    public ParticleEngine particleEngine;
    @Final
    @Shadow
    public KeyboardHandler keyboardHandler;
    @Shadow
    private Connection pendingConnection;
    @Shadow
    protected int missTime;

    public MinecraftMixin(String p_18765_) {
        super(p_18765_);
    }

    @Shadow
    protected abstract void handleKeybinds();

    @Shadow
    public HitResult hitResult;
    @Final
    @Shadow
    public Gui gui;

    @Shadow
    protected abstract boolean isMultiplayerServer();

    @Final
    @Shadow
    public LevelRenderer levelRenderer;
    @Final
    @Shadow private TextureManager textureManager;
    @Final
    @Shadow
    private Tutorial tutorial;


    @Shadow
    public abstract void setScreen(@Nullable Screen p_91153_);

    @Shadow
    public abstract void tick();

    @Shadow public abstract DebugScreenOverlay getDebugOverlay();

    @Inject(method = "runTick", at = @At("HEAD"))
    public void runTick(boolean p_91384_, CallbackInfo ci) {
        ITimer rider_api$timerTimer = (ITimer) this.timer;

        if (this.level != null) {
            int time_Status = Variables.getVariable(this.level, ModVariable.TIME_STATUS);
            if (time_Status == 1) {

                int i1;
                if (p_91384_) {
                    int j = rider_api$timerTimer.riderApi$advanceTime(Util.getMillis());

                    for (i1 = 0; i1 < Math.min(10, j); ++i1) {
                        this.rider_api$specialTickStart(time_Status);
                    }

                }

            }else if (time_Status == 2) {
                int i1;
                if (p_91384_) {
                    int j = rider_api$timerTimer.riderApi$advanceTime(Util.getMillis());

                    for (i1 = 0; i1 < Math.min(10, j); ++i1) {
                        this.tick();
                    }

                }
            }

            if (time_Status != 2) {
                this.soundManager.resume();
            } else{
                this.soundManager.pause();
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        if (this.level != null && (int) Variables.getVariable(this.level, ModVariable.TIME_STATUS) != 0) {
            boolean speed_down = true;

            int time_state = Variables.getVariable(this.level, ModVariable.TIME_STATUS);

            if (this.level != null && this.player != null) {
                if ((int) Variables.getVariable(this.level, ModVariable.TIME_STATUS) ==1)
                    speed_down = !GJ.TimerControl.isSlowEntity(this.player);
                else if ((int) Variables.getVariable(this.level, ModVariable.TIME_STATUS) ==2) {
                    speed_down = GJ.TimerControl.isPauseEntity(this.player);
                }
            }

            ClientHooks.fireClientTickPre();
            if (speed_down) {
                if (this.rightClickDelay > 0) {
                    --this.rightClickDelay;
                }
            }

            this.profiler.push("gui");

            if (speed_down) {
                this.gui.tick(this.pause);
            }

            this.profiler.pop();

            if (speed_down) {
                this.gameRenderer.pick(1.0F);
            }


            if (speed_down) {
                this.tutorial.onLookAt(this.level, this.hitResult);
            }

            this.profiler.push("gameMode");
            if (!this.pause && this.level != null) {
                if (speed_down) {
                    this.gameMode.tick();
                }
            }

            this.profiler.popPush("textures");
            if (this.level != null) {
                if (time_state != 2) {
                    this.textureManager.tick();
                }
            }


            if (speed_down) {
                if (this.screen == null && this.player != null) {
                    if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
                        this.setScreen(null);
                    } else if (this.player.isSleeping() && this.level != null) {
                        this.setScreen(new InBedChatScreen());
                    }
                } else {
                    Screen $$4 = this.screen;
                    if ($$4 instanceof InBedChatScreen) {
                        InBedChatScreen inbedchatscreen = (InBedChatScreen) $$4;
                        if (!this.player.isSleeping()) {
                            inbedchatscreen.onPlayerWokeUp();
                        }
                    }
                }
            }


            if (speed_down) {
                if (this.screen != null) {
                    this.missTime = 10000;
                }
            }


            if (this.screen != null) {
                Screen.wrapScreenError(() ->
                        this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
            }


            if (!this.getDebugOverlay().showDebugScreen()) {
                this.gui.clearCache();
            }


            if (speed_down) {
                if (this.overlay == null && (this.screen == null)) {
                    this.profiler.popPush("Keybindings");
                    this.handleKeybinds();
                    if (this.missTime > 0) {
                        --this.missTime;
                    }
                }
            }

            if (this.level != null) {
                this.profiler.popPush("gameRenderer");

                if (speed_down) {
                    if (!this.pause) {
                        this.gameRenderer.tick();
                    }
                }


                this.profiler.popPush("levelRenderer");
                if (!this.pause) {
                    this.levelRenderer.tick();
                }

                this.profiler.popPush("level");
                if (!this.pause) {
                    if (this.level.getSkyFlashTime() > 0) {
                        this.level.setSkyFlashTime(this.level.getSkyFlashTime() - 1);
                    }

                    this.level.tickEntities();
                }
            } else if (this.gameRenderer.currentEffect() != null) {
                if (time_state != 2) {
                    this.gameRenderer.shutdownEffect();
                }
            }

            if (!this.pause) {
                if (time_state != 2) {
                    this.musicManager.tick();
                }
            }

            if (time_state != 2) {
                this.soundManager.tick(this.pause);
            }
            if (this.level != null) {
                if (!this.pause) {
                    if (!this.options.joinedFirstServer && this.isMultiplayerServer()) {
                        Component component = Component.translatable ("tutorial.socialInteractions.title");
                        Component component1 = Component.translatable ("tutorial.socialInteractions.description", new Object[]{Tutorial.key("socialInteractions")});
                        this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, component, component1, true);
                        this.tutorial.addTimedToast(this.socialInteractionsToast, 160);
                        this.options.joinedFirstServer = true;
                        this.options.save();
                    }

                    this.tutorial.tick();
                    EventHooks.fireLevelTickPre(this.level, () -> true);

                    try {
                        this.level.tick(() -> true);
                    } catch (Throwable var4) {
                        CrashReport crashreport = CrashReport.forThrowable(var4, "Exception in world tick");
                        if (this.level == null) {
                            CrashReportCategory crashreportcategory = crashreport.addCategory("Affected level");
                            crashreportcategory.setDetail("Problem", "Level is null!");
                        } else {
                            this.level.fillReportDetails(crashreport);
                        }

                        throw new ReportedException(crashreport);
                    }

                    EventHooks.fireLevelTickPost(this.level, () -> true);
                }

                this.profiler.popPush("animateTick");
                if (!this.pause && this.level != null) {
                    this.level.animateTick(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
                }

                this.profiler.popPush("particles");
                if (!this.pause) {
                    this.particleEngine.tick();
                }
            } else if (this.pendingConnection != null) {
                this.profiler.popPush("pendingConnection");
                this.pendingConnection.tick();
            }

            this.profiler.popPush("keyboard");
            if (speed_down) {
                this.keyboardHandler.tick();
            }
            this.profiler.pop();
            ClientHooks.fireClientTickPost();
            ci.cancel();
        }
    }

    @Unique
    private void rider_api$specialTickStart(int time_Status) {

        if (this.level == null || this.player == null) return;

        boolean speed_down =! GJ.TimerControl.isSlowEntity(this.player);

        if (!speed_down) {
            if (this.rightClickDelay > 0) {
                --this.rightClickDelay;
            }
        }
        this.gameRenderer.pick(1.0F);
        this.tutorial.onLookAt(this.level, this.hitResult);

        this.gui.tick(this.pause || time_Status == 2);
        this.profiler.pop();

        if (this.screen == null && this.player != null) {
            if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
                this.setScreen(null);
            } else if (this.player.isSleeping() && this.level != null) {
                this.setScreen(new InBedChatScreen());
            }
        } else {
            Screen $$4 = this.screen;
            if ($$4 instanceof InBedChatScreen) {
                InBedChatScreen inbedchatscreen = (InBedChatScreen) $$4;
                if (!this.player.isSleeping()) {
                    inbedchatscreen.onPlayerWokeUp();
                }
            }
        }

        if (!speed_down) {
            if (this.overlay == null && (this.screen == null)) {

                this.handleKeybinds();
                if (this.missTime > 0) {
                    --this.missTime;
                }
            }
        }
        if (!speed_down) {
            if (this.screen != null) {
                this.missTime = 10000;
            }
        }

        if (speed_down) {
            if (this.screen != null) {
                Screen.wrapScreenError(() ->
                        this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
            }
        }

        if (!this.pause && this.level != null) {
            this.gameMode.tick();
        }
        if (!this.getDebugOverlay().showDebugScreen()) {
            this.gui.clearCache();
        }


        if (!this.pause || time_Status ==2) {
            this.gameRenderer.tick();
        }

        if (!speed_down) {
            this.keyboardHandler.tick();
        }

        if (this.level instanceof TimerClientLevel _level){
            if (time_Status == 1) {
                _level.rider_api$tickEntitiesTimeSlow();
            }
        }

    }
}
