package com.bawnorton.quickloot.render.screen;

import com.bawnorton.quickloot.keybind.KeybindManager;
import com.bawnorton.quickloot.render.RenderHelper;
import com.bawnorton.quickloot.util.ContainerStatus;
import com.bawnorton.quickloot.util.OptionalStackSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class QuickLootWidget {
    private static QuickLootWidget INSTANCE;

    private static final double ASPECT_RATIO = 1.5;
    private static final int ROW_HEIGHT = 18;

    private int HEIGHT;
    private int WIDTH;
    private int X_OFFSET;
    private int x;
    private int y;
    private int shownHeight;
    private int shownRows;

    private final Map<ItemStack, Integer> stacks;
    private final List<ItemStack> stackList = new ArrayList<>();

    private int selected;
    private int scrollOffset;
    private ContainerStatus status;

    public QuickLootWidget() {
        stacks = new HashMap<>();
        selected = -1;
        resetStatus();
    }

    public static QuickLootWidget getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new QuickLootWidget();
        }
        return INSTANCE;
    }

    public void updateItems(Map<ItemStack, Integer> stackSlotMap) {
        stacks.clear();
        stackList.clear();
        for(Map.Entry<ItemStack, Integer> entry : stackSlotMap.entrySet()) {
            ItemStack stack = entry.getKey();
            if(stack.isEmpty()) continue;
            int slot = entry.getValue();
            stacks.put(stack, slot);
            stackList.add(stack);
        }
        stackList.sort(Comparator.comparing(stack -> stack.getItem().getName().getString()));

        if(stacks.isEmpty()) {
            status.setEmpty(true);
        }
    }

    private void refresh() {
        Window window = MinecraftClient.getInstance().getWindow();
        int width = window.getScaledWidth();
        int height = window.getScaledHeight();
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        this.x = halfWidth - (WIDTH / 2);
        this.y = halfHeight - (HEIGHT / 2);
        scaleToFit(width, height, window.getScaleFactor());
        selected = MathHelper.clamp(selected, 0, stackList.size() - 1);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, stackList.size() - shownRows);
    }

    private void scaleToFit(int winWidth, int winHeight, double winScale) {
        HEIGHT = (int) (600 / winScale);
        WIDTH = (int) (HEIGHT * ASPECT_RATIO);
        X_OFFSET = (int) (WIDTH * 0.7);
        if(WIDTH > (winWidth - X_OFFSET) / 2 - 10) {
            WIDTH = (winWidth - X_OFFSET) / 2 - 10;
            X_OFFSET = (int) (WIDTH * 0.7);
            HEIGHT = (int) (WIDTH / ASPECT_RATIO);
        }
        if(HEIGHT > winHeight / 2 - 10) {
            HEIGHT = winHeight / 2 - 10;
            WIDTH = (int) (HEIGHT * ASPECT_RATIO);
        }
        if (stackList.size() >= (HEIGHT - 4) / ROW_HEIGHT) {
            shownHeight = HEIGHT;
        } else {
            shownHeight = stackList.size() * ROW_HEIGHT + 6;
        }
        shownRows = (shownHeight - 4) / ROW_HEIGHT;
    }

    private boolean shouldRender() {
        return MinecraftClient.getInstance().currentScreen == null;
    }

    public void render(MatrixStack matricies, String title) {
        if(!shouldRender()) return;
        refresh();
        matricies.push();
        matricies.translate(0, 0, 500);
        RenderHelper.drawOutlinedText(matricies, title, x + X_OFFSET + 4, y - 10, 0xFFFFFFFF, 0xFF000000);
        if(!status.isNormal()) {
            RenderHelper.drawBorderedArea(matricies, x + X_OFFSET, y, WIDTH, ROW_HEIGHT + 6, 2,0xAA000000, 0xFF000000);
            RenderHelper.drawText(matricies, status.getText(), x + X_OFFSET + 25, y + 8, 0xFFFFFFFF);
            RenderHelper.drawText(matricies, "♦", x + X_OFFSET + 10, y + 8, 0xFFFFFFFF);
            renderButtonHints(matricies, y + ROW_HEIGHT + 13);
            matricies.pop();
            return;
        }

        RenderHelper.drawBorderedArea(matricies, x + X_OFFSET, y, WIDTH, shownHeight, 2,0xAA000000, 0xFF000000);
        RenderHelper.startScissor(x + X_OFFSET + 2, y + 2, WIDTH - 6, shownHeight - 4);
        int row = 0;
        for(ItemStack stack : stackList) {
            int count = stack.getCount();
            String name = stack.getName().getString();
            int yOffset = (row - scrollOffset) * ROW_HEIGHT + 4;
            if(row == selected && status.hasTakeHint()) {
                RenderHelper.drawArea(matricies, x + X_OFFSET + 4, y + yOffset, WIDTH - 8, ROW_HEIGHT - 2, 0x55FFFFFF);
            }
            RenderHelper.drawItem(matricies, stack, x + X_OFFSET + 4, y + yOffset);
            RenderHelper.drawText(matricies, name + " (" + count + ")", x + X_OFFSET + 25, y + yOffset + 4, 0xFFFFFFFF);
            row++;
        }
        RenderHelper.endScissor();
        renderButtonHints(matricies, shownHeight + y + 7);
        matricies.pop();
    }

    private void renderButtonHints(MatrixStack matricies, int y) {
        if(!status.hasHint()) return;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        String lootKey = KeybindManager.getLootKeyString().toUpperCase();
        String openKey = KeybindManager.getOpenKeyString().toUpperCase();
        int lootKeyWidth = textRenderer.getWidth(lootKey);
        int openKeyWidth = textRenderer.getWidth(openKey);
        int fullWidth = Math.max(lootKeyWidth + textRenderer.getWidth("Take"), openKeyWidth + textRenderer.getWidth("Open")) + 9;
        int x = this.x + X_OFFSET + (WIDTH / 2) - (fullWidth / 2);

        if(status.hasTakeHint()) {
            RenderHelper.drawBorderedAreaAroundText(matricies, lootKey, x, y, 5, 2, 0xAA000000, 0xFF000000, 0xFFFFFFFF);
            RenderHelper.drawOutlinedText(matricies, "Take", x + lootKeyWidth + 9, y, 0xFFFFFFFF, 0xFF000000);
        }
        if(status.hasOpenHint()) {
            RenderHelper.drawBorderedAreaAroundText(matricies, openKey, x, y + (status.hasTakeHint() ? 20 : 0), 5, 2, 0xAA000000, 0xFF000000, 0xFFFFFFFF);
            RenderHelper.drawOutlinedText(matricies, "Open", x + openKeyWidth + 9, y + (status.hasTakeHint() ? 20 : 0), 0xFFFFFFFF, 0xFF000000);
        }
    }

    public OptionalStackSlot getSelectedItem() {
        if(selected == -1) return OptionalStackSlot.empty();
        if(stackList.isEmpty()) return OptionalStackSlot.empty();
        ItemStack stack = stackList.get(selected);
        int slot = stacks.get(stack);
        return OptionalStackSlot.of(stack, slot);
    }

    public ContainerStatus getStatus() {
        return status;
    }

    public void resetStatus() {
        status = ContainerStatus.normal();
    }

    public void block() {
        status.setBlocked(true);
    }

    public boolean isBlocked() {
        return status.isBlocked();
    }

    public boolean requiresSneaking() {
        return status.requiresSneaking();
    }

    public void next() {
        if(selected == stackList.size() - 1) {
            start();
        } else {
            selected++;
            if(selected > (scrollOffset + shownRows - 1)) {
                scrollOffset++;
            }
        }
    }

    public void previous() {
        if(selected == 0) {
            end();
        } else {
            selected--;
            if(selected < scrollOffset) {
                scrollOffset--;
            }
        }
    }

    public void end() {
        selected = stackList.size() - 1;
        scrollOffset = Math.max(0, stackList.size() - shownRows);
    }

    public void start() {
        selected = 0;
        scrollOffset = 0;
    }
}
