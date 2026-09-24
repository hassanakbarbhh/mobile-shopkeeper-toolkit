package com.shopkeeper.mobileshop.ui.sync

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.OutboxOperation
import com.shopkeeper.mobileshop.databinding.FragmentSyncCenterBinding
import com.shopkeeper.mobileshop.databinding.ItemOutboxOperationBinding
import com.shopkeeper.mobileshop.sync.SyncEngine
import com.shopkeeper.mobileshop.sync.SyncPreferences
import com.shopkeeper.mobileshop.sync.SyncState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncCenterFragment : Fragment() {

    private var _binding: FragmentSyncCenterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        val outboxDao = db.outboxDao()

        setupListeners()
        observeSyncState()
        observeMetrics(outboxDao)
        observeOutboxLog(outboxDao)
    }

    private fun setupListeners() {
        binding.btnSyncNow.setOnClickListener {
            SyncEngine.triggerSyncNow(requireContext())
            Toast.makeText(requireContext(), "Synchronizing Outbox with cloud...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeSyncState() {
        viewLifecycleOwner.lifecycleScope.launch {
            SyncEngine.syncState.collectLatest { state ->
                when (state) {
                    is SyncState.Synced -> {
                        binding.tvSyncStatusIcon.text = "🟢"
                        binding.tvSyncStatusTitle.text = "Connected & Synced"
                        binding.tvSyncStatusSubtitle.text = "All local Room transactions up to date"
                        binding.btnSyncNow.isEnabled = true
                        binding.btnSyncNow.text = "Sync Now"
                    }
                    is SyncState.Pending -> {
                        binding.tvSyncStatusIcon.text = "🔵"
                        binding.tvSyncStatusTitle.text = "Syncing with Cloud..."
                        binding.tvSyncStatusSubtitle.text = "${state.pendingCount} operations in transmission"
                        binding.btnSyncNow.isEnabled = false
                        binding.btnSyncNow.text = "Syncing..."
                    }
                    is SyncState.Offline -> {
                        binding.tvSyncStatusIcon.text = "🟠"
                        binding.tvSyncStatusTitle.text = "Offline Mode Active"
                        binding.tvSyncStatusSubtitle.text = "${state.pendingCount} operations queued locally in Room"
                        binding.btnSyncNow.isEnabled = true
                        binding.btnSyncNow.text = "Retry"
                    }
                    is SyncState.Error -> {
                        binding.tvSyncStatusIcon.text = "🔴"
                        binding.tvSyncStatusTitle.text = "Sync Interrupted"
                        binding.tvSyncStatusSubtitle.text = state.errorMessage
                        binding.btnSyncNow.isEnabled = true
                        binding.btnSyncNow.text = "Retry"
                    }
                }
            }
        }
    }

    private fun observeMetrics(outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao) {
        viewLifecycleOwner.lifecycleScope.launch {
            outboxDao.getPendingCount().collectLatest { count ->
                _binding?.tvCountPending?.text = count.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            outboxDao.getCompletedCount().collectLatest { count ->
                _binding?.tvCountUploaded?.text = count.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            outboxDao.getFailedCount().collectLatest { count ->
                _binding?.tvCountFailed?.text = count.toString()
            }
        }

        // Real Downloaded Count & Last Sync Timestamp
        val downloaded = SyncPreferences.getDownloadedCount(requireContext())
        _binding?.tvCountDownloaded?.text = downloaded.toString()

        val lastSync = SyncPreferences.getLastSyncTimestamp(requireContext())
        if (lastSync > 0) {
            val dateFmt = SimpleDateFormat("h:mm:ss a, d MMM yyyy", Locale.getDefault())
            _binding?.tvLastSyncTime?.text = "Last successful sync: " + dateFmt.format(Date(lastSync))
        } else {
            _binding?.tvLastSyncTime?.text = "Last sync: Ready to synchronize"
        }
    }

    private fun observeOutboxLog(outboxDao: com.shopkeeper.mobileshop.data.db.dao.OutboxDao) {
        val timeFmt = SimpleDateFormat("h:mm a, d MMM", Locale.getDefault())

        viewLifecycleOwner.lifecycleScope.launch {
            outboxDao.getRecentOperations(50).collectLatest { operations ->
                val container = _binding?.layoutOutboxItems ?: return@collectLatest
                val emptyView = _binding?.layoutEmptyOutbox ?: return@collectLatest
                container.removeAllViews()

                if (operations.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    return@collectLatest
                }
                emptyView.visibility = View.GONE

                for (op in operations) {
                    val itemBinding = ItemOutboxOperationBinding.inflate(layoutInflater, container, false)
                    itemBinding.tvOpTitle.text = "${op.operationType} (${op.entityType} #${op.entityId})"
                    itemBinding.tvOpTime.text = "${timeFmt.format(Date(op.createdAt))} • ${op.retryCount} retries"

                    when (op.status) {
                        OutboxOperation.STATUS_COMPLETED -> {
                            itemBinding.tvOpStatusIcon.text = "✓"
                            itemBinding.tvOpStatusIcon.setTextColor(Color.parseColor("#059669"))
                            itemBinding.tvOpBadge.text = "COMPLETED"
                            itemBinding.tvOpBadge.setTextColor(Color.parseColor("#065F46"))
                            itemBinding.tvOpBadge.setBackgroundResource(R.drawable.bg_badge_green)
                        }
                        OutboxOperation.STATUS_UPLOADING -> {
                            itemBinding.tvOpStatusIcon.text = "▲"
                            itemBinding.tvOpStatusIcon.setTextColor(Color.parseColor("#2563EB"))
                            itemBinding.tvOpBadge.text = "UPLOADING"
                            itemBinding.tvOpBadge.setTextColor(Color.parseColor("#1D4ED8"))
                            itemBinding.tvOpBadge.setBackgroundResource(R.drawable.bg_pill_white_10)
                        }
                        OutboxOperation.STATUS_PENDING -> {
                            itemBinding.tvOpStatusIcon.text = "⏳"
                            itemBinding.tvOpStatusIcon.setTextColor(Color.parseColor("#D97706"))
                            itemBinding.tvOpBadge.text = "PENDING"
                            itemBinding.tvOpBadge.setTextColor(Color.parseColor("#92400E"))
                            itemBinding.tvOpBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                        }
                        OutboxOperation.STATUS_FAILED -> {
                            itemBinding.tvOpStatusIcon.text = "✕"
                            itemBinding.tvOpStatusIcon.setTextColor(Color.parseColor("#DC2626"))
                            itemBinding.tvOpBadge.text = "FAILED"
                            itemBinding.tvOpBadge.setTextColor(Color.parseColor("#991B1B"))
                            itemBinding.tvOpBadge.setBackgroundResource(R.drawable.bg_badge_red)
                        }
                    }
                    container.addView(itemBinding.root)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
