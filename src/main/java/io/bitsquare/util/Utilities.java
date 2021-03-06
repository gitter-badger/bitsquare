/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.net.URI;

import java.util.function.Function;

import javafx.animation.AnimationTimer;
import javafx.scene.input.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.mightypork.rpack.utils.DesktopApi;

/**
 * General utilities
 */
public class Utilities {
    private static final Logger log = LoggerFactory.getLogger(Utilities.class);
    private static long lastTimeStamp = System.currentTimeMillis();

    public static String objectToJson(Object object) {
        Gson gson =
                new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).setPrettyPrinting().create();
        return gson.toJson(object);
    }

    public static boolean isWindows() {
        return getOSName().contains("win");
    }

    public static boolean isOSX() {
        return getOSName().contains("mac") || getOSName().contains("darwin");
    }

    public static boolean isLinux() {
        return getOSName().contains("linux");
    }

    private static String getOSName() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static void copyToClipboard(String content) {
        if (content != null && content.length() > 0) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
        }
    }

    public static void openURI(URI uri) throws IOException {
        if (!isLinux()
                && Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri);
        }
        else {
            // On Linux Desktop is poorly implemented. 
            // See https://stackoverflow.com/questions/18004150/desktop-api-is-not-supported-on-the-current-platform
            if (!DesktopApi.browse(uri))
                throw new IOException("Failed to open URI: " + uri.toString());
        }
    }

    public static void openWebPage(String target) throws Exception {
        openURI(new URI(target));
    }


    public static <T> T jsonToObject(String jsonString, Class<T> classOfT) {
        Gson gson =
                new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).setPrettyPrinting().create();
        return gson.fromJson(jsonString, classOfT);
    }


    public static Object deserializeHexStringToObject(String serializedHexString) {
        Object result = null;
        try {
            ByteArrayInputStream byteInputStream =
                    new ByteArrayInputStream(org.bitcoinj.core.Utils.parseAsHexOrBase58(serializedHexString));

            try (ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream)) {
                result = objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                byteInputStream.close();

            }

        } catch (IOException i) {
            i.printStackTrace();
        }
        return result;
    }


    public static String serializeObjectToHexString(Serializable serializable) {
        String result = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(serializable);

            result = org.bitcoinj.core.Utils.HEX.encode(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            objectOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void printElapsedTime(String msg) {
        if (!msg.isEmpty()) {
            msg += " / ";
        }
        long timeStamp = System.currentTimeMillis();
        log.debug(msg + "Elapsed: " + String.valueOf(timeStamp - lastTimeStamp));
        lastTimeStamp = timeStamp;
    }

    public static void printElapsedTime() {
        printElapsedTime("");
    }


    public static Object copy(Serializable orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static AnimationTimer setTimeout(int delay, Function<AnimationTimer, Void> callback) {
        AnimationTimer animationTimer = new AnimationTimer() {
            final long lastTimeStamp = System.currentTimeMillis();

            @Override
            public void handle(long arg0) {
                if (System.currentTimeMillis() > delay + lastTimeStamp) {
                    callback.apply(this);
                    this.stop();
                }
            }
        };
        animationTimer.start();
        return animationTimer;
    }

    public static AnimationTimer setInterval(int delay, Function<AnimationTimer, Void> callback) {
        AnimationTimer animationTimer = new AnimationTimer() {
            long lastTimeStamp = System.currentTimeMillis();

            @Override
            public void handle(long arg0) {
                if (System.currentTimeMillis() > delay + lastTimeStamp) {
                    lastTimeStamp = System.currentTimeMillis();
                    callback.apply(this);
                }
            }
        };
        animationTimer.start();
        return animationTimer;
    }
}
