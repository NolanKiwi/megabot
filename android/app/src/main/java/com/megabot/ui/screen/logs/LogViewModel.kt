package com.megabot.ui.screen.logs

import androidx.lifecycle.ViewModel
import com.megabot.data.local.db.dao.MessageLogDao
import com.megabot.data.local.db.entity.MessageLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    messageLogDao: MessageLogDao
) : ViewModel() {
    val logs: Flow<List<MessageLogEntity>> = messageLogDao.getRecentLogs(200)
}
