@Database(
    entities = [
        ContainerEntity::class,
        DocumentEntity::class,
        AttachmentEntity::class,
        ReminderEntity::class,
        DocumentKitEntity::class,
        KitItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun containerDao(): ContainerDao

    abstract fun documentDao(): DocumentDao

    abstract fun attachmentDao(): AttachmentDao

    abstract fun reminderDao(): ReminderDao

    abstract fun documentKitDao(): DocumentKitDao

    abstract fun kitItemDao(): KitItemDao
}