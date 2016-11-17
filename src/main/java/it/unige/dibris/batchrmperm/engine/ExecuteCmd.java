package it.unige.dibris.batchrmperm.engine;

import it.unige.dibris.batchrmperm.exception.OutputEmptyException;

import java.io.*;

import java.util.*;
import java.util.concurrent.TimeUnit;



public class ExecuteCmd {
    private static final String EMULATOR = "/home/simo/android-sdk-linux/tools/emulator";
    private static final String ADB = "/home/simo/android-sdk-linux/platform-tools/adb";
    private final String device;

    public ExecuteCmd(String device) {
        this.device = device;
    }

    public void returnToHomeScreen() throws IOException, InterruptedException {
        String[] args = {ADB, "-s", device, "shell", "am", "start", "-a", "android.intent.action.MAIN",
                "-c", "android.intent.category.HOME"};
        execute(Arrays.asList(args));
    }


    /**
     *
     * @param packageName
     * @return true if the app crashed, false otherwise
     * @throws IOException
     * @throws InterruptedException
     */
    public List<String> testMonkey(String packageName) throws IOException, InterruptedException, OutputEmptyException {
        List<String> args = new ArrayList<>();
        args.add(ADB);
        args.add("-s");
        args.add(device);
        args.add("shell");
        args.add("monkey");
        args.add("-p");
        args.add(packageName);
        args.add("-vv");
        args.add("512");
        List<String> output = execute(args);
        if (output.isEmpty())
            throw new OutputEmptyException();
        return output;
    }


    public void uninstallApk(String packageName) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(ADB);
        args.add("-s");
        args.add(device);
        args.add("uninstall");
        args.add(packageName);
        List<String> output = execute(args);
        if (!output.isEmpty()) {
            if (output.get(0).equals("Success")) {
                System.out.println("Successfully uninstalled: " + packageName);
            }
            else
                System.err.println("Failure uninstall: " + packageName);

        }
    }


    public static void startAdbServer() throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(ADB);
        args.add("start-server");
        execute(args, true, false);
    }


    public void rebootDevice() throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(ADB);
        args.add("-s");
        args.add(device);
        args.add("shell");
        args.add("reboot");
        execute(args, true, false);
        Thread.sleep(30000);
    }


    public static List<String> devicesAttached() throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(ADB);
        args.add("devices");
        List<String> output = execute(args);
        output.remove("");
        output.remove("List of devices attached");
        for (final ListIterator<String> i = output.listIterator(); i.hasNext();) {
            final String element = i.next();
            i.set(element.replaceAll("\\tdevice", ""));
        }
        return output;
    }


    public List<String> installApk(String apkPath) throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(ADB);
        args.add("-s");
        args.add(device);
        args.add("install");
        args.add("-r");
        args.add(apkPath);
        return execute(args);
    }


    public static void startEmulator() throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add(EMULATOR);
        args.add("-avd");
        args.add("Nexus_5_API_22");
        args.add("-wipe-data");
        execute(args, true, false);
        Thread.sleep(27000);
    }


    private static List<String> execute(List<String> args) throws IOException, InterruptedException {
        return execute(args, true, true);
    }


    private static List<String> execute(List<String> args, boolean getInputStream, boolean waitFor) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start();
        InputStream is;
        if (getInputStream)
            is = process.getInputStream();
        else
            is = process.getErrorStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        if (waitFor) {
            List<String> output = new LinkedList<>();
            process.waitFor(3, TimeUnit.MINUTES);
            //process.waitFor();
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
            }
            return output;
        }
        return null;
    }

}
