package com.dp.dawalo.utils

import android.view.View
import com.facebook.shimmer.ShimmerFrameLayout

object LoadingHelper {
    
    /**
     * Show shimmer loading animation
     */
    fun showShimmer(shimmerLayout: ShimmerFrameLayout?, contentView: View?) {
        shimmerLayout?.visibility = View.VISIBLE
        shimmerLayout?.startShimmer()
        contentView?.visibility = View.GONE
    }
    
    /**
     * Hide shimmer and show content
     */
    fun hideShimmer(shimmerLayout: ShimmerFrameLayout?, contentView: View?) {
        shimmerLayout?.stopShimmer()
        shimmerLayout?.visibility = View.GONE
        contentView?.visibility = View.VISIBLE
    }
    
    /**
     * Show progress bar
     */
    fun showProgress(progressBar: View?) {
        progressBar?.visibility = View.VISIBLE
    }
    
    /**
     * Hide progress bar
     */
    fun hideProgress(progressBar: View?) {
        progressBar?.visibility = View.GONE
    }
    
    /**
     * Enable/disable view with loading state
     */
    fun setViewEnabled(view: View?, enabled: Boolean) {
        view?.isEnabled = enabled
        view?.alpha = if (enabled) 1.0f else 0.5f
    }
}
