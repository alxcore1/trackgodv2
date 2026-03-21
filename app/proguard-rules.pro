# Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Suppress Room paging warnings
-dontwarn androidx.room.paging.**
