package net.zxxxc.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;

public class PulseWatermarkRenderer {
    // Текстура фиолетового пульса
    private static final Identifier PULSE_ICON = Identifier.of("zxxxcvisuals", "textures/pulse_logo.png");

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            // Считаем FPS и Пинг
            int fps = client.getCurrentFps();
            int ping = 0;
            PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (playerListEntry != null) {
                ping = playerListEntry.getLatency();
            }

            // Текст нашей капсулы
            String title = "zxxxcvisuals.pro";
            String pingText = ping + " ms";
            String fpsText = fps + " FPS";
            String separator = "  /  ";

            int textWidth = client.textRenderer.getWidth(title) 
                    + client.textRenderer.getWidth(separator) 
                    + client.textRenderer.getWidth(pingText) 
                    + client.textRenderer.getWidth(separator) 
                    + client.textRenderer.getWidth(fpsText);

            // Позиционирование и геометрия
            int x = 10;
            int y = 10;
            int iconWidth = 14;
            int padding = 8;
            int spacing = 6;
            int height = 22;
            int width = padding + iconWidth + spacing + textWidth + padding;

            // Отрисовка закругленной капсулы (HEX: #0D0E11, Альфа: ~90%)
            drawRoundedRect(drawContext, x, y, x + width, y + height, height / 2f, 0xE50D0E11);

            // Отрисовка фиолетового значка пульса
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            int iconY = y + (height - iconWidth) / 2;
            drawContext.drawTexture(PULSE_ICON, x + padding, iconY, 0, 0, iconWidth, iconWidth, iconWidth, iconWidth);

            // Рендер текста с тусклыми разделителями
            int currentX = x + padding + iconWidth + spacing;
            int textY = y + (height - 8) / 2;

            drawContext.drawText(client.textRenderer, title, currentX, textY, 0xFFFFFF, false);
            currentX += client.textRenderer.getWidth(title);

            drawContext.drawText(client.textRenderer, separator, currentX, textY, 0x55FFFFFF, false);
            currentX += client.textRenderer.getWidth(separator);

            drawContext.drawText(client.textRenderer, pingText, currentX, textY, 0xFFFFFF, false);
            currentX += client.textRenderer.getWidth(pingText);

            drawContext.drawText(client.textRenderer, separator, currentX, textY, 0x55FFFFFF, false);
            currentX += client.textRenderer.getWidth(separator);

            drawContext.drawText(client.textRenderer, fpsText, currentX, textY, 0xFFFFFF, false);
        });
    }

    // Алгоритм построения гладких углов через Triangle Fan
    private static void drawRoundedRect(DrawContext context, int x1, int y1, int x2, int y2, float radius, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        int[][] corners = {
            {x2 - (int)radius, y1 + (int)radius, 270, 360},
            {x2 - (int)radius, y2 - (int)radius, 0, 90},
            {x1 + (int)radius, y2 - (int)radius, 90, 180},
            {x1 + (int)radius, y1 + (int)radius, 180, 270}
        };

        for (int[] corner : corners) {
            int cx = corner[0];
            int cy = corner[1];
            int startAngle = corner[2];
            int endAngle = corner[3];

            for (int i = startAngle; i <= endAngle; i += 5) {
                double radians = Math.toRadians(i);
                float sin = (float) Math.sin(radians) * radius;
                float cos = (float) Math.cos(radians) * radius;
                bufferBuilder.vertex(context.getMatrices().peek().getPositionMatrix(), cx + cos, cy + sin, 0).color(r, g, b, a);
            }
        }
        
        BufferByteData data = bufferBuilder.end();
        if (data != null) {
            BufferRenderer.drawWithGlobalProgram(data);
        }
        RenderSystem.disableBlend();
    }
}
