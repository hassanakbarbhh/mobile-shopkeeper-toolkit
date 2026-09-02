package com.shopkeeper.mobileshop.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.databinding.ViewSyncStatusIndicatorBinding
import com.shopkeeper.mobileshop.sync.GitHubSyncManager
import com.shopkeeper.mobileshop.sync.SyncState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SyncStatusIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSyncStatusIndicatorBinding =
        ViewSyncStatusIndicatorBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentState: SyncState = SyncState.Synced(System.currentTimeMillis())
    private var customSyncListener: (() -> Unit)? = null

    init {
        setupClickListeners()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Automatically observe GitHubSyncManager flow when attached to lifecycle
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            GitHubSyncManager.syncState.collectLatest { state ->
                renderState(state)
            }
        } ?: renderState(GitHubSyncManager.syncState.value)
    }

    private fun setupClickListeners() {
        binding.btnSyncAction.setOnClickListener {
            if (currentState is SyncState.Pending) return@setOnClickListener
            if (customSyncListener != null) {
                customSyncListener?.invoke()
            } else {
                triggerSync()
            }
        }

        binding.cardSyncRoot.setOnClickListener {
            showSyncDetailsDialog()
        }
    }

    fun setOnSyncClickListener(listener: () -> Unit) {
        this.customSyncListener = listener
    }

    fun triggerSync() {
        GitHubSyncManager.triggerSync(context)
    }

    fun setSyncState(state: SyncState) {
        renderState(state)
    }

    private fun renderState(state: SyncState) {
        currentState = state
        val repo = GitHubSyncManager.getRepoName(context)
        val branch = GitHubSyncManager.getBranchName(context)
        binding.tvRepoLabel.text = "GitHub: $repo • branch: $branch"

        when (state) {
            is SyncState.Synced -> {
                // Background and icons
                binding.vStatusBg.setBackgroundResource(R.drawable.bg_badge_green)
                binding.ivStatusIcon.visibility = View.VISIBLE
                binding.ivStatusIcon.setImageResource(R.drawable.ic_sync_done)
                binding.pbSyncProgress.visibility = View.GONE

                // Badge
                binding.tvStatusBadge.text = "SYNCED"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.green_800))

                // Titles & Subtitles
                binding.tvStatusTitle.text = "Room ⇄ GitHub: Synced"
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(context, R.color.green_900))
                val timeStr = GitHubSyncManager.formatTimestamp(state.lastSyncTimestamp)
                val countInfo = if (state.syncedItemsCount > 0) " (${state.syncedItemsCount} entities)" else ""
                binding.tvStatusSubtitle.text = "Last synced: $timeStr • commit ${state.commitSha}$countInfo"

                // Button
                binding.btnSyncAction.isEnabled = true
                binding.btnSyncAction.text = "Sync"
                binding.btnSyncAction.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.green_100)
                )
                binding.btnSyncAction.setTextColor(ContextCompat.getColor(context, R.color.green_900))

                // Error
                binding.llErrorDetails.visibility = View.GONE
                binding.cardSyncRoot.strokeColor = ContextCompat.getColor(context, R.color.green_800)
            }

            is SyncState.Pending -> {
                // Background and progress
                binding.vStatusBg.setBackgroundResource(R.drawable.bg_badge_amber)
                binding.ivStatusIcon.visibility = View.GONE
                binding.pbSyncProgress.visibility = View.VISIBLE

                // Badge
                binding.tvStatusBadge.text = "PENDING"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.amber_800))

                // Titles & Subtitles
                binding.tvStatusTitle.text = "Room ⇄ GitHub: Syncing..."
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(context, R.color.amber_800))
                binding.tvStatusSubtitle.text = state.message

                // Button
                binding.btnSyncAction.isEnabled = false
                binding.btnSyncAction.text = "Syncing..."
                binding.btnSyncAction.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.amber_100)
                )
                binding.btnSyncAction.setTextColor(ContextCompat.getColor(context, R.color.amber_800))

                // Error
                binding.llErrorDetails.visibility = View.GONE
                binding.cardSyncRoot.strokeColor = ContextCompat.getColor(context, R.color.amber_800)
            }

            is SyncState.Error -> {
                // Background and icons
                binding.vStatusBg.setBackgroundResource(R.drawable.bg_badge_red)
                binding.ivStatusIcon.visibility = View.VISIBLE
                binding.ivStatusIcon.setImageResource(R.drawable.ic_sync_error)
                binding.pbSyncProgress.visibility = View.GONE

                // Badge
                binding.tvStatusBadge.text = "ERROR"
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_red)
                binding.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.red_800))

                // Titles & Subtitles
                binding.tvStatusTitle.text = "Room ⇄ GitHub: Sync Error"
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(context, R.color.red_800))
                val timeStr = GitHubSyncManager.formatTimestamp(state.lastAttemptTimestamp)
                binding.tvStatusSubtitle.text = "Last failed attempt: $timeStr"

                // Error Details Banner
                binding.llErrorDetails.visibility = View.VISIBLE
                binding.tvErrorMessage.text = state.errorMessage

                // Button
                binding.btnSyncAction.isEnabled = state.canRetry
                binding.btnSyncAction.text = "Retry"
                binding.btnSyncAction.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.red_100)
                )
                binding.btnSyncAction.setTextColor(ContextCompat.getColor(context, R.color.red_800))

                binding.cardSyncRoot.strokeColor = ContextCompat.getColor(context, R.color.red_800)
            }
        }
    }

    private fun showSyncDetailsDialog() {
        val repo = GitHubSyncManager.getRepoName(context)
        val branch = GitHubSyncManager.getBranchName(context)

        val stateInfo = when (val s = currentState) {
            is SyncState.Synced -> """
                Status: Synced (OK)
                Last Synced: ${GitHubSyncManager.formatTimestamp(s.lastSyncTimestamp)}
                Commit SHA: ${s.commitSha}
                Synced Room Entities: ${s.syncedItemsCount}
            """.trimIndent()
            is SyncState.Pending -> """
                Status: Pending / In Progress
                Active Task: ${s.message}
                Pending Entities: ${s.pendingCount}
            """.trimIndent()
            is SyncState.Error -> """
                Status: Error (Sync Interrupted)
                Last Attempt: ${GitHubSyncManager.formatTimestamp(s.lastAttemptTimestamp)}
                Failure Details: ${s.errorMessage}
            """.trimIndent()
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Room ⇄ GitHub Sync Status")
            .setIcon(R.drawable.ic_github)
            .setMessage("""
                Target Repository:
                $repo (branch: $branch)
                
                Database Engine:
                Android Room (SQLite) Local Store
                
                $stateInfo
            """.trimIndent())
            .setPositiveButton("Sync Now") { _, _ ->
                triggerSync()
            }
            .setNeutralButton("Dismiss", null)
            .show()
    }
}
