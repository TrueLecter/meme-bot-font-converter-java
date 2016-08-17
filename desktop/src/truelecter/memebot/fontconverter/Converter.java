package truelecter.memebot.fontconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter.FontInfo;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter.Padding;
import com.badlogic.gdx.utils.GdxNativesLoader;

public class Converter {

    public static void main(String[] args) throws IOException {
        GdxNativesLoader.load();
        if (args.length < 1) {
            System.out.println("Need font name");
            System.exit(1);
        }
        String fontname = args[0];
        String chars = null;
        String currentDir = Paths.get("").toAbsolutePath().toString();
        // String runnablePath = URLDecoder
        // .decode(Converter.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
        // "UTF-8");
        if (args.length > 1) {
            chars = new String(Files.readAllBytes(new File(args[1]).toPath()),
                    Charset.forName("UTF-8"));
        } else {
            String newLine = System.getProperty("line.separator");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Converter.class.getResourceAsStream("chars.txt")));
            StringBuilder result = new StringBuilder();
            String line;
            boolean flag = false;
            while ((line = reader.readLine()) != null) {
                result.append(flag ? newLine : "").append(line);
                flag = true;
            }
            chars = result.toString();
        }

        int initialSize = 8;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                new FileHandle(currentDir + File.separator + fontname + ".ttf"));

        JSONObject fontJson = new JSONObject();

        fontJson.put("name", fontname);

        JSONArray fontSizes = new JSONArray();

        BitmapFontWriter.setOutputFormat(BitmapFontWriter.OutputFormat.XML);
        
        for (int i = 0; i < 8; i++) {

            FontInfo info = new FontInfo();
            info.padding = new Padding(0, 0, 0, 0);

            FreeTypeFontParameter param = new FreeTypeFontParameter();
            param.size = initialSize * (i + 1);
            param.borderWidth = 2;
            param.gamma = 2f;
            param.shadowOffsetY = 1;
            param.renderCount = 3;
            param.characters = chars;
            param.packer = new PixmapPacker(4096, 4096, Format.RGBA8888, 2, false, new PixmapPacker.SkylineStrategy());

            FreeTypeBitmapFontData data = generator.generateData(param);

            BitmapFontWriter.writeFont(data, new String[] { "font" + i + ".png" },
                    new FileHandle(currentDir + File.separator + fontname + File.separator + "font" + i + ".fnt"), info,
                    4096, 4096);
            BitmapFontWriter.writePixmaps(param.packer.getPages(),
                    new FileHandle(currentDir + File.separator + fontname + File.separator), "font" + i);

            fontSizes.put(new JSONObject().put("size", param.size).put("filename", "font" + i + ".fnt"));
        }

        fontJson.put("sizes", fontSizes);

        fontJson.write(new FileWriter(currentDir + File.separator + fontname + File.separator + "font.json")).close();;

        System.exit(0);
    }
}
