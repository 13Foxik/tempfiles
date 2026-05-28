/*
 * EatTogether — блок-схема алгоритма работы приложения.
 *
 * Программа без внешних зависимостей: загружает Noto Sans с системы и
 * одновременно рендерит блок-схему в PNG (через Graphics2D) и в SVG
 * (через ручную сборку XML).
 *
 * Геометрия и тексты — единый источник правды: все методы draw* пишут и в
 * растр, и в SVG.
 */
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.imageio.ImageIO;

public class RenderFlowchart {

    // ---------- глобальные настройки ----------
    static final int W = 2400;
    static final int H = 1700;

    static final Color BORDER       = new Color(0x1F, 0x74, 0x4D);
    static final Color BORDER_DARK  = new Color(0x14, 0x4F, 0x35);
    static final Color FILL_RECT    = Color.WHITE;
    static final Color FILL_OVAL    = new Color(0xE8, 0xF5, 0xEE);
    static final Color FILL_DIAMOND = new Color(0xFF, 0xF6, 0xDC);
    static final Color FILL_PARA    = new Color(0xFD, 0xEC, 0xEC);
    static final Color TEXT         = new Color(0x1A, 0x1A, 0x1A);
    static final Color ARROW        = new Color(0x14, 0x4F, 0x35);

    static final float STROKE_BOX = 2.5f;
    static final float STROKE_ARR = 2.2f;

    // ---------- состояние рендера ----------
    static BufferedImage img;
    static Graphics2D g;
    static StringBuilder svg;
    static Font fontMain;
    static Font fontDecision;
    static Font fontLabel;
    static FontRenderContext frc;

    // ---------- запуск ----------
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");

        // загружаем системный Noto Sans (есть на этой машине)
        Font noto = Font.createFont(Font.TRUETYPE_FONT,
                new File("/usr/share/fonts/google-noto-vf/NotoSans[wght].ttf"));
        fontMain     = noto.deriveFont(Font.PLAIN, 22f);
        fontDecision = noto.deriveFont(Font.BOLD,  22f);
        fontLabel    = noto.deriveFont(Font.BOLD,  20f);

        img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);
        frc = g.getFontRenderContext();

        svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ");
        svg.append("viewBox=\"0 0 ").append(W).append(" ").append(H).append("\" ");
        svg.append("width=\"").append(W).append("\" height=\"").append(H).append("\" ");
        svg.append("font-family=\"'Noto Sans','PT Sans','Segoe UI',Arial,sans-serif\">\n");
        svg.append("  <defs>\n");
        svg.append("    <marker id=\"arr\" viewBox=\"0 0 12 12\" refX=\"10\" refY=\"6\" ")
           .append("markerWidth=\"10\" markerHeight=\"10\" orient=\"auto-start-reverse\">\n");
        svg.append("      <path d=\"M0,0 L12,6 L0,12 z\" fill=\"#144F35\"/>\n");
        svg.append("    </marker>\n");
        svg.append("  </defs>\n");
        svg.append("  <rect width=\"").append(W).append("\" height=\"").append(H)
           .append("\" fill=\"#FFFFFF\"/>\n");

        drawAll();

        svg.append("</svg>\n");

        File out = new File("/projects/sandbox/tempfiles/diagrams");
        out.mkdirs();
        Path png = Path.of(out.getAbsolutePath(), "algorithm_flowchart.png");
        Path svgFile = Path.of(out.getAbsolutePath(), "algorithm_flowchart.svg");
        ImageIO.write(img, "PNG", png.toFile());
        Files.writeString(svgFile, svg.toString());

        System.out.println("PNG: " + png);
        System.out.println("SVG: " + svgFile);
    }

    // ============================================================
    //                     LAYOUT
    // ============================================================
    static void drawAll() {
        // координаты-центры (cx) и y-верх (yTop) каждого блока.
        // Текст блока — массив строк.

        // ---------- начало / запуск ----------
        Pt nachalo = oval(1200, 30, 240, 70, new String[]{"Начало"});
        Pt zapusk  = rect(1200, 130, 540, 90,
                new String[]{"Запуск приложения и подключение",
                             "к Cloud Firestore"});
        Pt avtor   = diamond(1200, 260, 420, 150,
                new String[]{"Пользователь",
                             "авторизован?"});

        // ---------- авторизация (LEFT) ----------
        Pt auth1 = rect(280, 290, 460, 90,
                new String[]{"Отображение экрана",
                             "регистрации и авторизации"});
        Pt auth2 = rect(280, 410, 460, 90,
                new String[]{"Ввод адреса электронной",
                             "почты и пароля"});
        Pt auth3 = diamond(280, 540, 380, 140,
                new String[]{"Авторизация",
                             "успешна?"});
        Pt authErr = parallelogram(750, 565, 320, 90,
                new String[]{"Сообщение об ошибке",
                             "авторизации"});

        // ---------- загрузка пользователя / семья ----------
        Pt loadUser = rect(1200, 720, 600, 90,
                new String[]{"Загрузка сведений о пользователе",
                             "из Cloud Firestore"});
        Pt family   = diamond(1200, 850, 480, 150,
                new String[]{"Принадлежит",
                             "к семейной группе?"});

        // ---------- семейная подсхема (RIGHT) ----------
        Pt action  = diamond(1900, 850, 380, 150,
                new String[]{"Действие",
                             "пользователя?"});
        Pt famNew  = rect(1700, 1030, 460, 130,
                new String[]{"Создание семейной группы,",
                             "генерация идентификатора,",
                             "назначение роли «администратор»"});
        Pt famJoin1 = rect(2120, 1030, 340, 80,
                new String[]{"Ввод идентификатора",
                             "семейной группы"});
        Pt famJoin2 = rect(2120, 1130, 340, 80,
                new String[]{"Формирование заявки на",
                             "вступление и ожидание принятия"});
        Pt famJoin3 = rect(2120, 1230, 340, 80,
                new String[]{"Назначение пользователю",
                             "роли «участник»"});

        // ---------- меню / горизонтальный цикл ----------
        Pt loadMenu = rect(1200, 1340, 580, 90,
                new String[]{"Загрузка структуры меню",
                             "(категорий, подкатегорий, блюд)"});
        Pt mainScr  = rect(380, 1490, 360, 90,
                new String[]{"Отображение",
                             "главного экрана"});
        Pt scenario = rect(820, 1490, 400, 90,
                new String[]{"Выполнение выбранного",
                             "сценария взаимодействия"});
        Pt update   = rect(1260, 1490, 400, 90,
                new String[]{"Обновление сведений",
                             "в Cloud Firestore"});
        Pt loop     = diamond(1700, 1465, 320, 140,
                new String[]{"Продолжить",
                             "работу?"});
        Pt finish   = rect(2100, 1490, 360, 90,
                new String[]{"Завершение работы",
                             "приложения"});
        Pt konec    = oval(2100, 1605, 240, 70, new String[]{"Конец"});

        // ============================================================
        //                  СТРЕЛКИ
        // ============================================================
        // главная вертикаль (верх → авторизация-решение → загрузка → семья)
        arrowDown(nachalo.bottom, zapusk.top, null);
        arrowDown(zapusk.bottom, avtor.topV, null);

        // авторизация: решение → ветка «Нет» (LEFT) и «Да» (вниз по центру)
        arrowOrth(avtor.leftV, auth1.right, "Нет");
        // «Да» — короткая прямая вниз по центральной оси к «Загрузка сведений»
        arrowDown(avtor.bottomV, loadUser.top, "Да");

        // подсхема авторизации (LEFT)
        arrowDown(auth1.bottom, auth2.top, null);
        arrowDown(auth2.bottom, auth3.topV, null);
        arrowOrth(auth3.rightV, authErr.left, "Нет");
        // петля «Сообщение об ошибке» → возврат сверху в «Ввод адреса»
        arrowOrthMulti(new int[][]{
                authErr.top,                  // (750, 565)
                {authErr.top[0], 395},        // вверх в зазор между Auth1 и Auth2
                {auth2.top[0],   395},        // влево к оси auth-колонки
                auth2.top                     // вниз в верх «Ввод адреса»
        }, null);
        // «Да» от Auth3 → к «Загрузка сведений» (через нижний обход слева)
        arrowOrthMulti(new int[][]{
                auth3.bottomV,                // (280, 680)
                {auth3.bottomV[0], loadUser.left[1]},  // (280, 765)
                loadUser.left                 // (900, 765)
        }, "Да");

        // загрузка пользователя → семья
        arrowDown(loadUser.bottom, family.topV, null);

        // семья: решение → «Да» (вниз к меню) и «Нет» (RIGHT в подсхему)
        arrowOrth(family.rightV, action.leftV, "Нет");
        // «Да» — длинная вертикаль до «Загрузка структуры меню»
        arrowDownLong(family.bottomV, loadMenu.top, "Да");

        // подсхема семьи (RIGHT)
        // действие → «Создание» (влево-вниз)
        arrowOrthMulti(new int[][]{
                action.bottomV,
                {action.bottomV[0], 1010},
                {famNew.top[0],     1010},
                famNew.top
        }, null);
        drawLabel(1800, 1003, "Создание");
        // действие → «Вступление» (вправо-вниз)
        arrowOrthMulti(new int[][]{
                action.bottomV,
                {action.bottomV[0], 1010},
                {famJoin1.top[0],   1010},
                famJoin1.top
        }, null);
        drawLabel(2010, 1003, "Вступление");
        arrowDown(famJoin1.bottom, famJoin2.top, null);
        arrowDown(famJoin2.bottom, famJoin3.top, null);

        // обе ветви семьи → «Загрузка структуры меню» через правый край
        // ветвь «Создание»: вниз и налево к правому краю «Загрузка структуры меню»
        arrowOrthMulti(new int[][]{
                famNew.bottom,                  // (1700, 1160)
                {famNew.bottom[0], loadMenu.right[1]}, // (1700, 1385)
                loadMenu.right                   // (1490, 1385)
        }, null);
        // ветвь «Вступление»: вниз и налево к тому же узлу
        arrowOrthMulti(new int[][]{
                famJoin3.bottom,                            // (2120, 1310)
                {famJoin3.bottom[0], loadMenu.right[1]},    // (2120, 1385)
                loadMenu.right                              // (1490, 1385)
        }, null);

        // загрузка меню → отображение главного экрана (через изгиб влево)
        arrowOrthMulti(new int[][]{
                loadMenu.bottom,
                {loadMenu.bottom[0], 1460},
                {mainScr.top[0],     1460},
                mainScr.top
        }, null);

        // горизонталь: главный экран → сценарий → обновление → продолжить?
        arrowRight(mainScr.right, scenario.left, null);
        arrowRight(scenario.right, update.left, null);
        arrowRight(update.right,   loop.leftV,   null);

        // петля «Да»: верх ромба → влево над цепочкой → вниз в главный экран
        arrowOrthMulti(new int[][]{
                loop.topV,
                {loop.topV[0],   1440},
                {mainScr.top[0], 1440},
                mainScr.top
        }, "Да");

        // «Нет»: ромб → завершение → конец
        arrowRight(loop.rightV, finish.left, "Нет");
        arrowDown(finish.bottom, konec.top, null);
    }

    // ============================================================
    //          ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ И МЕТОДЫ ОТРИСОВКИ
    // ============================================================
    /** Точки-«якоря» блока для подключения стрелок. */
    static class Pt {
        int[] top, bottom, left, right;       // середина каждой стороны (rect/oval)
        int[] topV, bottomV, leftV, rightV;   // вершины ромба
    }

    static Pt rect(int cx, int yTop, int w, int h, String[] lines) {
        return rect(cx, yTop, w, h, lines, FILL_RECT, false);
    }

    static Pt rect(int cx, int yTop, int w, int h, String[] lines,
                   Color fill, boolean rounded) {
        int x = cx - w / 2;
        int radius = rounded ? 32 : 8;
        Shape s = new RoundRectangle2D.Float(x, yTop, w, h, radius, radius);

        g.setColor(fill);
        g.fill(s);
        g.setStroke(new BasicStroke(STROKE_BOX, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.setColor(BORDER);
        g.draw(s);

        svg.append(String.format(Locale.ROOT,
                "  <rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" rx=\"%d\" ry=\"%d\" "
                        + "fill=\"#%06X\" stroke=\"#%06X\" stroke-width=\"%.1f\"/>\n",
                x, yTop, w, h, radius, radius,
                fill.getRGB() & 0xFFFFFF, BORDER.getRGB() & 0xFFFFFF, STROKE_BOX));
        drawCenteredLines(cx, yTop + h / 2, lines, fontMain);

        Pt p = new Pt();
        p.top    = new int[]{cx,       yTop};
        p.bottom = new int[]{cx,       yTop + h};
        p.left   = new int[]{x,        yTop + h / 2};
        p.right  = new int[]{x + w,    yTop + h / 2};
        return p;
    }

    static Pt oval(int cx, int yTop, int w, int h, String[] lines) {
        int x = cx - w / 2;
        Shape s = new Ellipse2D.Float(x, yTop, w, h);

        g.setColor(FILL_OVAL);
        g.fill(s);
        g.setStroke(new BasicStroke(STROKE_BOX, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.setColor(BORDER);
        g.draw(s);

        svg.append(String.format(Locale.ROOT,
                "  <ellipse cx=\"%d\" cy=\"%d\" rx=\"%d\" ry=\"%d\" "
                        + "fill=\"#%06X\" stroke=\"#%06X\" stroke-width=\"%.1f\"/>\n",
                cx, yTop + h / 2, w / 2, h / 2,
                FILL_OVAL.getRGB() & 0xFFFFFF, BORDER.getRGB() & 0xFFFFFF, STROKE_BOX));
        drawCenteredLines(cx, yTop + h / 2, lines, fontDecision);

        Pt p = new Pt();
        p.top    = new int[]{cx,    yTop};
        p.bottom = new int[]{cx,    yTop + h};
        p.left   = new int[]{x,     yTop + h / 2};
        p.right  = new int[]{x + w, yTop + h / 2};
        return p;
    }

    static Pt diamond(int cx, int yTop, int w, int h, String[] lines) {
        int hw = w / 2;
        int hh = h / 2;
        int cy = yTop + hh;
        Path2D.Float p = new Path2D.Float();
        p.moveTo(cx,      yTop);
        p.lineTo(cx + hw, cy);
        p.lineTo(cx,      yTop + h);
        p.lineTo(cx - hw, cy);
        p.closePath();

        g.setColor(FILL_DIAMOND);
        g.fill(p);
        g.setStroke(new BasicStroke(STROKE_BOX, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.setColor(BORDER);
        g.draw(p);

        svg.append(String.format(Locale.ROOT,
                "  <polygon points=\"%d,%d %d,%d %d,%d %d,%d\" "
                        + "fill=\"#%06X\" stroke=\"#%06X\" stroke-width=\"%.1f\"/>\n",
                cx, yTop, cx + hw, cy, cx, yTop + h, cx - hw, cy,
                FILL_DIAMOND.getRGB() & 0xFFFFFF, BORDER.getRGB() & 0xFFFFFF, STROKE_BOX));
        drawCenteredLines(cx, cy, lines, fontDecision);

        Pt out = new Pt();
        out.topV    = new int[]{cx,        yTop};
        out.bottomV = new int[]{cx,        yTop + h};
        out.leftV   = new int[]{cx - hw,   cy};
        out.rightV  = new int[]{cx + hw,   cy};
        return out;
    }

    static Pt parallelogram(int cx, int yTop, int w, int h, String[] lines) {
        int hw = w / 2;
        int slant = 26;
        Path2D.Float p = new Path2D.Float();
        p.moveTo(cx - hw + slant, yTop);
        p.lineTo(cx + hw,         yTop);
        p.lineTo(cx + hw - slant, yTop + h);
        p.lineTo(cx - hw,         yTop + h);
        p.closePath();

        g.setColor(FILL_PARA);
        g.fill(p);
        g.setStroke(new BasicStroke(STROKE_BOX, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.setColor(BORDER);
        g.draw(p);

        svg.append(String.format(Locale.ROOT,
                "  <polygon points=\"%d,%d %d,%d %d,%d %d,%d\" "
                        + "fill=\"#%06X\" stroke=\"#%06X\" stroke-width=\"%.1f\"/>\n",
                cx - hw + slant, yTop, cx + hw, yTop,
                cx + hw - slant, yTop + h, cx - hw, yTop + h,
                FILL_PARA.getRGB() & 0xFFFFFF, BORDER.getRGB() & 0xFFFFFF, STROKE_BOX));
        drawCenteredLines(cx, yTop + h / 2, lines, fontMain);

        Pt out = new Pt();
        out.top    = new int[]{cx,        yTop};
        out.bottom = new int[]{cx,        yTop + h};
        out.left   = new int[]{cx - hw + slant / 2, yTop + h / 2};
        out.right  = new int[]{cx + hw - slant / 2, yTop + h / 2};
        return out;
    }

    static void drawCenteredLines(int cx, int cy, String[] lines, Font font) {
        g.setFont(font);
        g.setColor(TEXT);
        int n = lines.length;
        // высота строки ~ font ascent + descent + leading
        TextLayout sample = new TextLayout("Aj", font, frc);
        float lineH = sample.getAscent() + sample.getDescent() + sample.getLeading() + 2;
        float totalH = lineH * n;
        float startY = cy - totalH / 2f + sample.getAscent();
        for (int i = 0; i < n; i++) {
            String line = lines[i];
            TextLayout layout = new TextLayout(line, font, frc);
            float lw = (float) layout.getBounds().getWidth();
            float x = cx - lw / 2f - (float) layout.getBounds().getX();
            float y = startY + i * lineH;
            layout.draw(g, x, y);

            String escaped = xmlEsc(line);
            String fontWeight = (font.isBold() ? "bold" : "normal");
            float svgY = y;
            int textColor = TEXT.getRGB() & 0xFFFFFF;
            svg.append(String.format(Locale.ROOT,
                    "  <text x=\"%d\" y=\"%.1f\" font-size=\"%.0f\" "
                            + "font-weight=\"%s\" fill=\"#%06X\" "
                            + "text-anchor=\"middle\">%s</text>\n",
                    cx, svgY, (float) font.getSize2D(), fontWeight, textColor, escaped));
        }
    }

    // ---------- стрелки ----------
    static void arrowDown(int[] from, int[] to, String label) {
        // ровно вертикальная линия
        line(from[0], from[1], to[0], to[1], true);
        if (label != null) {
            int midY = (from[1] + to[1]) / 2;
            drawLabel(from[0] + 16, midY, label);
        }
    }

    static void arrowRight(int[] from, int[] to, String label) {
        line(from[0], from[1], to[0], to[1], true);
        if (label != null) {
            int midX = (from[0] + to[0]) / 2;
            drawLabel(midX, from[1] - 12, label);
        }
    }

    static void arrowOrth(int[] from, int[] to, String label) {
        // L-образное соединение: горизонталь, потом вертикаль (или наоборот).
        // Если одна координата совпадает — рисуем одну прямую линию со стрелкой.
        int dx = to[0] - from[0];
        int dy = to[1] - from[1];
        if (dx == 0 || dy == 0) {
            line(from[0], from[1], to[0], to[1], true);
        } else if (Math.abs(dx) >= Math.abs(dy)) {
            line(from[0], from[1], to[0], from[1], false);
            line(to[0],   from[1], to[0], to[1],   true);
        } else {
            line(from[0], from[1], from[0], to[1], false);
            line(from[0], to[1],   to[0],   to[1], true);
        }
        if (label != null) {
            int lx = from[0] + (dx >= 0 ? 14 : -14);
            int ly = from[1] - 12;
            // если стрелка идёт чисто горизонтально — поднимем подпись над линией
            if (dy == 0) {
                lx = (from[0] + to[0]) / 2;
                ly = from[1] - 14;
            }
            drawLabel(lx, ly, label);
        }
    }

    static void arrowOrthMulti(int[][] pts, String label) {
        // Многосегментная орто-ломаная: рисуем все сегменты, кроме последнего,
        // обычной линией; последний — со стрелкой.
        for (int i = 0; i < pts.length - 1; i++) {
            int[] a = pts[i];
            int[] b = pts[i + 1];
            line(a[0], a[1], b[0], b[1], i == pts.length - 2);
        }
        if (label != null) {
            int[] a = pts[0];
            // подпись возле начала
            drawLabel(a[0] + 14, a[1] + 18, label);
        }
    }

    static void arrowDownLong(int[] from, int[] to, String label) {
        // длинная вертикаль с возможным небольшим горизонтальным выходом
        if (from[0] == to[0]) {
            line(from[0], from[1], to[0], to[1], true);
        } else {
            int midY = (from[1] + to[1]) / 2;
            line(from[0], from[1], from[0], midY, false);
            line(from[0], midY,    to[0],   midY, false);
            line(to[0],   midY,    to[0],   to[1], true);
        }
        if (label != null) {
            drawLabel(from[0] + 14, from[1] + 18, label);
        }
    }

    // ---------- низкоуровневая отрисовка линии и стрелки ----------
    static void line(int x1, int y1, int x2, int y2, boolean withArrow) {
        g.setColor(ARROW);
        g.setStroke(new BasicStroke(STROKE_ARR, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Float(x1, y1, x2, y2));

        svg.append(String.format(Locale.ROOT,
                "  <line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" "
                        + "stroke=\"#%06X\" stroke-width=\"%.1f\" "
                        + "stroke-linecap=\"round\"%s/>\n",
                x1, y1, x2, y2, ARROW.getRGB() & 0xFFFFFF, STROKE_ARR,
                withArrow ? " marker-end=\"url(#arr)\"" : ""));

        if (withArrow) {
            drawArrowhead(x1, y1, x2, y2);
        }
    }

    static void drawArrowhead(int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len < 0.01) return;
        double ux = dx / len;
        double uy = dy / len;
        double size = 14;
        double base = 7;

        double bx = x2 - ux * size;
        double by = y2 - uy * size;
        double leftX  = bx - uy * base;
        double leftY  = by + ux * base;
        double rightX = bx + uy * base;
        double rightY = by - ux * base;

        Path2D.Double head = new Path2D.Double();
        head.moveTo(x2, y2);
        head.lineTo(leftX, leftY);
        head.lineTo(rightX, rightY);
        head.closePath();
        g.setColor(ARROW);
        g.fill(head);
        // стрелочка в SVG уже добавлена через marker-end; в PNG — отрисовали выше.
    }

    static void drawLabel(int cx, int cy, String text) {
        g.setFont(fontLabel);
        g.setColor(TEXT);
        TextLayout layout = new TextLayout(text, fontLabel, frc);
        float lw = (float) layout.getBounds().getWidth();
        float lh = (float) layout.getBounds().getHeight();
        float pad = 6;
        // фон-плашка
        Shape pill = new RoundRectangle2D.Float(
                cx - lw / 2 - pad, cy - lh - 2,
                lw + pad * 2, lh + pad * 2,
                8, 8);
        g.setColor(new Color(0xFF, 0xFF, 0xFF, 0xE0));
        g.fill(pill);
        g.setColor(TEXT);
        layout.draw(g, cx - lw / 2 - (float) layout.getBounds().getX(), cy);

        svg.append(String.format(Locale.ROOT,
                "  <rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" "
                        + "rx=\"6\" ry=\"6\" fill=\"#FFFFFF\" fill-opacity=\"0.85\"/>\n",
                cx - lw / 2 - pad, cy - lh - 2, lw + pad * 2, lh + pad * 2));
        svg.append(String.format(Locale.ROOT,
                "  <text x=\"%d\" y=\"%d\" font-size=\"%.0f\" font-weight=\"bold\" "
                        + "fill=\"#1A1A1A\" text-anchor=\"middle\">%s</text>\n",
                cx, cy, (float) fontLabel.getSize2D(), xmlEsc(text)));
    }

    static String xmlEsc(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
