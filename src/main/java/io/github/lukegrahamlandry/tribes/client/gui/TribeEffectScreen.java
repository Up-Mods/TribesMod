package io.github.lukegrahamlandry.tribes.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lukegrahamlandry.tribes.api.tribe.EffectsInfo;
import io.github.lukegrahamlandry.tribes.init.NetworkHandler;
import io.github.lukegrahamlandry.tribes.init.TribesMobEffectTags;
import io.github.lukegrahamlandry.tribes.network.SaveEffectsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class TribeEffectScreen extends TribeScreen {
    // Confirmation button
    private ConfirmButton confirmButton;
    // List of all positive and negative effects
    public List<MobEffect> posEffects = TribesMobEffectTags.getGoodEffects();
    public List<MobEffect> negEffects = TribesMobEffectTags.getBadEffects();
    // Map of selected effects and their amplifiers
    private Map<MobEffect, Integer> selGoodEffects;
    private Map<MobEffect, Integer> selBadEffects;
    // Maximum number of effects that can be selected; Both good and bad
    private int maxGoodEffects;
    private int maxBadEffects;
    // Current number of selected effects
    private static int numSelectedGood;
    private static int numSelectedBad;

    int page = 0;
    private Button backButton;
    private Button nextButton;
    final int EFFECTS_PER_PAGE = 14;

    public TribeEffectScreen(int numGoodAllowed, int numBadAllowed, EffectsInfo effectsInfo) {
        super(".tribeEffectScreen", "textures/gui/tribe_effects_left.png", "textures/gui/tribe_effects_right.png", 175, 219, false);

        // all this stuff is sent from the server by PacketOpenEffectGUI

        this.maxGoodEffects = numGoodAllowed;
        this.maxBadEffects = numBadAllowed;

        selGoodEffects = new HashMap<>();
        selBadEffects = new HashMap<>();
        effectsInfo.getEffects().forEach((effect) -> {
            if (posEffects.contains(effect.effect())) selGoodEffects.put(effect.effect(), effect.level());
            if (negEffects.contains(effect.effect())) selBadEffects.put(effect.effect(), effect.level());
        });
        calcNumSelected();
    }

    @Override
    public void tick() {
        calcNumSelected();
        confirmButton.active = numSelectedGood == maxGoodEffects && numSelectedBad == maxBadEffects;
    }

    @Override
    protected void init() {
        super.init();
        this.backButton = this.addRenderableWidget(new Button(this.guiLeft + (this.xSize - 11), (this.ySize / 2 - 11) + 30, 20, 20, new TextComponent("<"), (p_214318_1_) -> {
            if (this.backButton.active) {
                this.page--;

                TribeEffectScreen.this.clearWidgets();
                TribeEffectScreen.this.init();
                TribeEffectScreen.this.tick();
            }
        }));
        this.backButton.active = this.page > 0;
        this.nextButton = this.addRenderableWidget(new Button(this.guiLeft + (this.xSize - 11), (this.ySize / 2 - 11) + 60, 20, 20, new TextComponent(">"), (p_214318_1_) -> {
            if (this.nextButton.active) {
                this.page++;

                TribeEffectScreen.this.clearWidgets();
                TribeEffectScreen.this.init();
                TribeEffectScreen.this.tick();
            }
        }));
        int shown = ((this.page + 1) * EFFECTS_PER_PAGE);
        this.nextButton.active = shown < posEffects.size() || shown < negEffects.size();


        // Declare confirm button
        this.confirmButton = this.addRenderableWidget(new ConfirmButton(this, this.guiLeft + (this.xSize - 11), (this.ySize / 2 - 11), this.ySize));

        int shift = this.page * EFFECTS_PER_PAGE;
        int i = 0;
        int k = 0;
        // Iteration through effects to create three buttons for three tiers of each effect

        for (int e = 0; e < EFFECTS_PER_PAGE; e++) {
            int index = shift + e;
            if (index >= posEffects.size()) break;
            MobEffect effect = posEffects.get(index);

            EffectButton tribeeffect$effectbutton;
            i = (k >= 154) ? 82 : i;
            k = (k >= 154) ? 0 : k;
            for (int j = 1; j <= 3; j++) {
                tribeeffect$effectbutton = new EffectButton(this, this.guiLeft + 11 + i, this.guiTop + 36 + k, this.ySize, effect, true, j);
                this.addRenderableWidget(tribeeffect$effectbutton);
                // Is the effect selected already?
                if (selGoodEffects.containsKey(effect) && selGoodEffects.get(effect) == j) {
                    tribeeffect$effectbutton.setSelected(true);
                } else {
                    tribeeffect$effectbutton.setSelected(false);
                }
                i += 22;
            }
            i -= 22 * 3;
            k += 22;

        }

        i = 0;
        k = 0;
        for (int e = 0; e < EFFECTS_PER_PAGE; e++) {
            int index = shift + e;
            if (index >= negEffects.size()) break;
            MobEffect effect = negEffects.get(index);

            i = (k >= 154) ? 82 : i;
            k = (k >= 154) ? 0 : k;
            for (int j = 1; j <= 3; j++) {
                var effectButton = new EffectButton(this, this.guiLeft + this.xSize + 16 + i, this.guiTop + 36 + k, ySize, effect, false, j);
                effectButton.setSelected(selBadEffects.containsKey(effect) && selBadEffects.get(effect) == j);
                this.addRenderableWidget(effectButton);
                i += 22;
            }
            i -= 22 * 3;
            k += 22;
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        this.font.draw(matrixStack, "Benefits: " + numSelectedGood + "/" + maxGoodEffects, this.guiLeft + 15, this.guiTop + 20, 0x5d5d5d);
        this.font.draw(matrixStack, "Drawbacks: " + numSelectedBad + "/" + maxBadEffects, this.guiLeft + 20 + xSize, this.guiTop + 20, 0x5d5d5d);

        for (Widget widget : this.renderables) {
            if(widget instanceof GuiButton button && button.isHoveredOrFocused()) {
                button.renderToolTip(matrixStack, mouseX, mouseY);
            }
        }
    }

    // Add effect to selected list
    private void addEffect(MobEffect effect, int amplifier, boolean isGood) {
        if (isGood) {
            selGoodEffects.put(effect, amplifier);
        } else {
            selBadEffects.put(effect, amplifier);
        }
        calcNumSelected();
    }

    // Remove effect from selected list
    private void removeEffect(MobEffect effect, int amplifier, boolean isGood) {
        if (isGood) {
            selGoodEffects.remove(effect, amplifier);
        } else {
            selBadEffects.remove(effect, amplifier);
        }
        calcNumSelected();
    }

    private void calcNumSelected() {
        numSelectedBad = 0;
        numSelectedGood = 0;

        Set<MobEffect> seen = new HashSet<>();
        var singleLevelEffects = ForgeRegistries.MOB_EFFECTS.tags().getTag(TribesMobEffectTags.SINGLE_LEVEL_EFFECTS);

        selGoodEffects.forEach((effect, integer) -> {

            if(singleLevelEffects.contains(effect)) {
                integer = 1;
            }

            if (!seen.contains(effect)) {
                numSelectedGood += integer;
                seen.add(effect);
            }
        });
        selBadEffects.forEach((effect, integer) -> {
            if (singleLevelEffects.contains(effect)) {
                integer = 1;
            }

            if (!seen.contains(effect)) {
                numSelectedBad += integer;
                seen.add(effect);
            }
        });
    }

    // Confirmation Button(Check Button)
    @OnlyIn(Dist.CLIENT)
    class ConfirmButton extends GuiButton.SpriteButton {
        TribeScreen screen;

        public ConfirmButton(TribeScreen screen, int x, int y, int ySizeIn) {
            super(screen, x, y, 90, 220, ySizeIn);
            this.screen = screen;
        }

        public void onPress() {
            if (this.active) {
                NetworkHandler.INSTANCE.sendToServer(new SaveEffectsPacket(selGoodEffects, selBadEffects));
                selBadEffects.clear();
                selGoodEffects.clear();
                TribeEffectScreen.this.minecraft.setScreen(null);
            }
        }

        public void renderToolTip(PoseStack matrixStack, int mouseX, int mouseY) {
            screen.renderTooltip(matrixStack, CommonComponents.GUI_DONE, mouseX, mouseY);
        }

        @Override
        public void updateNarration(NarrationElementOutput p_169152_) {

        }
    }

    // Effect Button
    @OnlyIn(Dist.CLIENT)
    class EffectButton extends GuiButton {
        private final TribeEffectScreen screen;
        private final MobEffect effect;
        private final boolean isGood;
        private final TextureAtlasSprite effectSprite;
        private final Component effectName;
        private final int amplifier;

        public EffectButton(TribeScreen screen, int x, int y, int ySizeIn, MobEffect effect, boolean isGoodIn, int amplifierIn) {
            super(screen, x, y, ySizeIn);
            this.effect = effect;
            this.isGood = isGoodIn;
            this.effectSprite = Minecraft.getInstance().getMobEffectTextures().get(effect);
            this.effectName = this.getEffectName(effect);
            this.screen = (TribeEffectScreen) screen;
            this.amplifier = amplifierIn;
        }

        // Get the name of the effect based on the amplifier
        private Component getEffectName(MobEffect effect) {

            if (ForgeRegistries.MOB_EFFECTS.tags().getTag(TribesMobEffectTags.SINGLE_LEVEL_EFFECTS).contains(effect)) {
                return new TranslatableComponent(effect.getDescriptionId());
            }

            ; // TODO: this is a hack to get the name of the effect without the amplifier
            return new TranslatableComponent(effect.getDescriptionId()).append(" " + "I".repeat(this.getAmplifier()));
        }

        public void onPress() {
            if (this.isSelected()) {
                TribeEffectScreen.this.removeEffect(effect, amplifier, isGood);
            } else {
                if (this.isGood) {
                    // Are the maximum number of effects selected?
                    if (TribeEffectScreen.this.selGoodEffects.size() < TribeEffectScreen.this.maxGoodEffects && (numSelectedGood + amplifier <= maxGoodEffects)) {
                        TribeEffectScreen.this.addEffect(effect, amplifier, true);
                    }
                    // Are you selecting a different level of a selected effect?
                    if (TribeEffectScreen.this.selGoodEffects.containsKey(effect) && (numSelectedGood + amplifier <= maxGoodEffects)) {
                        TribeEffectScreen.this.removeEffect(effect, TribeEffectScreen.this.selGoodEffects.get(effect), true);
                        TribeEffectScreen.this.addEffect(effect, amplifier, true);
                    }
                } else {
                    if (TribeEffectScreen.this.selBadEffects.size() < TribeEffectScreen.this.maxBadEffects && (numSelectedBad + amplifier <= maxBadEffects)) {
                        TribeEffectScreen.this.addEffect(effect, amplifier, false);
                    }
                    if (TribeEffectScreen.this.selBadEffects.containsKey(effect) && (numSelectedBad + amplifier <= maxBadEffects)) {
                        TribeEffectScreen.this.removeEffect(effect, TribeEffectScreen.this.selBadEffects.get(effect), false);
                        TribeEffectScreen.this.addEffect(effect, amplifier, false);
                    }
                }
            }
            TribeEffectScreen.this.clearWidgets();
            TribeEffectScreen.this.init();
            TribeEffectScreen.this.tick();
        }

        @Override
        public void renderToolTip(PoseStack matrixStack, int mouseX, int mouseY) {
            screen.renderTooltip(matrixStack, this.effectName, mouseX, mouseY);
        }

        public int getAmplifier() {
            return amplifier;
        }

        @Override
        protected void renderIcon(PoseStack matrixStack) {
            TribeEffectScreen.this.font.draw(matrixStack, String.valueOf(this.getAmplifier()), (float) (this.x + 2), (float) (this.y + 2), 0xffffff);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, this.effectSprite.atlas().location());
            blit(matrixStack, this.x + 2, this.y + 2, this.getBlitOffset(), 18, 18, this.effectSprite);
        }

        @Override
        public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

        }
    }
}
