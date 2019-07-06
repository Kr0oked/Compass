# Remove verbose logging
-assumenosideeffects class android.util.Log {
    public static *** v(...);
}
