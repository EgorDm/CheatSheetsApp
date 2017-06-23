package net.egordmitriev.cheatsheets.utils;

import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import net.egordmitriev.cheatsheets.CheatSheets;
import net.egordmitriev.cheatsheets.pojo.CacheData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Date;

import static net.egordmitriev.cheatsheets.api.API.sGson;
import static net.egordmitriev.cheatsheets.utils.Constants.CACHE_LIFETIME;

/**
 * Created by EgorDm on 12-Jun-2017.
 */

//TODO network

public class Utils {

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static void applyTextSpanWorkaround(TextView textView) {
        int padding = 12;
        textView.setShadowLayer(padding /* radius */, 0, 0, 0 /* transparent */);
        textView.setPadding(padding, padding, padding, padding);
    }

    public static <T> T readCache(String filename, Type type) {
        File file = new File(CheatSheets.getAppContext().getCacheDir(), filename+".json");
        if (file.exists()) {
            Type retType = TypeToken.getParameterized(CacheData.class, type).getType();
            CacheData<T> data = sGson.fromJson(Utils.readFile(file), retType);
            if (data.expires.before(new Date())) { //TODO: not connected = skip
                file.delete();
            } else {
                return data.data;
            }
        }
        return null;
    }

    public static <T> boolean writeCache(T data, String filename) {
        Date expires = new Date();
        expires.setTime(expires.getTime() + CACHE_LIFETIME);
        CacheData<T> cacheData = new CacheData<>(expires, data);
        boolean success;

        File file = new File(CheatSheets.getAppContext().getCacheDir(), filename+".json");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputWriter);
            bufferedWriter.write(sGson.toJson(cacheData));
            bufferedWriter.close();
            outputWriter.close();
            outputStream.close();
            success = true;
        } catch(Exception ex) {
            ex.printStackTrace();
            success = false;
        }
        return success;
    }

    public static String readFile(File file) {
        StringBuilder ret = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                ret.append(line);
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return ret.toString();
    }
}
