package org.ajcm.hiad.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhonlimaster on 22-07-16.
 */
public class FileUtils {

    public static String getStringNumber(int intNumber){
        String number = String.valueOf(intNumber);
        if (number.length() == 1) {
            number = "00" + number;
        } else if (number.length() == 2) {
            number = "0" + number;
        }
        return number;
    }

    public static File getDirHimnos(Context context){
        File dirHimnos = new File(context.getFilesDir().getAbsolutePath() + "/himnos/");
        dirHimnos.mkdir();
        return dirHimnos;
    }

    public static ArrayList<Integer> getHimnosDownloaded(Context context){
        ArrayList<Integer> integers = new ArrayList<>();
        int number = 0;
        for (File f : getDirHimnos(context).listFiles()) {
            if (f.getName().substring(0, 1).equalsIgnoreCase("0")) {
                if (f.getName().substring(1, 2).equalsIgnoreCase("0")) {
                    number = Integer.parseInt(String.valueOf(f.getName().charAt(2)));
                } else {
                    number = Integer.parseInt(f.getName().substring(1, 3));
                }
            } else {
                number = Integer.parseInt(f.getName().substring(0, 3));
            }
            integers.add(number);
        }
        return integers;
    }

    public static boolean isHimnoDownloaded(Context context, int number){
        ArrayList<Integer> himnosDownloaded = getHimnosDownloaded(context);
        return himnosDownloaded.contains(number);
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
