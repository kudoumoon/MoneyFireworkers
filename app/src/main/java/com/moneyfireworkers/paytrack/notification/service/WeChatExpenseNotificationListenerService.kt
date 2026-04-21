package com.moneyfireworkers.paytrack.notification.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.moneyfireworkers.paytrack.app.PayTrackApp
import com.moneyfireworkers.paytrack.notification.coordination.NotificationCandidateCoordinator
import com.moneyfireworkers.paytrack.notification.model.WeChatNotificationPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WeChatExpenseNotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName != WECHAT_PACKAGE) return
        val extras = sbn.notification.extras ?: return
        val payload = WeChatNotificationPayload(
            packageName = sbn.packageName,
            title = extras.getCharSequence("android.title")?.toString(),
            text = extras.getCharSequence("android.text")?.toString(),
            subText = extras.getCharSequence("android.subText")?.toString(),
            bigText = extras.getCharSequence("android.bigText")?.toString(),
            postedAt = sbn.postTime,
        )
        if (payload.combinedText.isBlank()) return

        val appContainer = (application as? PayTrackApp)?.appContainer ?: return
        Log.d(TAG, "WeChat notification received: ${payload.combinedText}")
        serviceScope.launch {
            val candidate = appContainer.processWeChatNotificationUseCase(payload)
            if (candidate != null) {
                Log.d(TAG, "Candidate prepared: amount=${candidate.amountInCent}, merchant=${candidate.merchantName}, pendingId=${candidate.pendingActionId}")
                NotificationCandidateCoordinator.present(candidate)
            } else {
                Log.d(TAG, "Notification ignored or failed to produce candidate.")
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "MFW-WeChatListener"
        const val WECHAT_PACKAGE = "com.tencent.mm"
    }
}
