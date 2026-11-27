package com.iancovski.hush.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.TestScope
import org.junit.rules.TemporaryFolder
import java.io.File

/** A real Preferences DataStore in a temporary file, tied to the test's lifetime. */
fun TestScope.testDataStore(folder: TemporaryFolder): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        scope = backgroundScope,
        produceFile = { File(folder.root, "settings.preferences_pb") },
    )
