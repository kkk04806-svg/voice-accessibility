package com.vanraj.assistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.List;
import java.util.Locale;

public class AppResolver {

    private final Context context;
    private final PackageManager pm;

    public AppResolver(Context context) {
        this.context = context.getApplicationContext();
        this.pm = context.getPackageManager();
    }

    public boolean openApp(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String wanted = name
                .toLowerCase(Locale.ROOT)
                .replace(" app", "")
                .trim();

        // Common aliases
        String packageName = getKnownPackage(wanted);

        if (packageName != null) {
            Intent intent = pm.getLaunchIntentForPackage(packageName);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        }

        // Dynamic installed-app search
        List<ApplicationInfo> apps =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {

            if (app == null) continue;

            String label = String.valueOf(
                    pm.getApplicationLabel(app)
            );

            String lowerLabel =
                    label.toLowerCase(Locale.ROOT);

            if (lowerLabel.equals(wanted) ||
                    lowerLabel.contains(wanted)) {

                Intent intent =
                        pm.getLaunchIntentForPackage(app.packageName);

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }
            }
        }

        // Package-name fallback
        for (ApplicationInfo app : apps) {

            if (app == null) continue;

            String pkg =
                    app.packageName.toLowerCase(Locale.ROOT);

            if (pkg.contains(wanted.replace(" ", ""))) {

                Intent intent =
                        pm.getLaunchIntentForPackage(app.packageName);

                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return true;
                }
            }
        }

        return false;
    }

    private String getKnownPackage(String name) {

        switch (name) {

            case "whatsapp":
                return "com.whatsapp";

            case "telegram":
                return "org.telegram.messenger";

            case "chrome":
                return "com.android.chrome";

            case "youtube":
                return "com.google.android.youtube";

            case "gmail":
                return "com.google.android.gm";

            case "maps":
            case "google maps":
            case "map":
                return "com.google.android.apps.maps";

            case "calculator":
            case "calc":
                return "com.vivo.calculator";

            case "instagram":
                return "com.instagram.android";

            case "facebook":
                return "com.facebook.katana";

            case "spotify":
                return "com.spotify.music";

            case "snapchat":
                return "com.snapchat.android";

            case "outlook":
                return "com.microsoft.office.outlook";

            case "play store":
            case "playstore":
                return "com.android.vending";

            default:
                return null;
        }
    }
}
