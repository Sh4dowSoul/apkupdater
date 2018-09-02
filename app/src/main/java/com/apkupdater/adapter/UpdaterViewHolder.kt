package com.apkupdater.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.apkupdater.R
import com.apkupdater.activity.MainActivity
import com.apkupdater.model.*
import com.apkupdater.util.*
import com.github.yeriomin.playstoreapi.GooglePlayException
import kotlinx.android.synthetic.main.updater_item_new.view.*
import uy.kohesive.injekt.api.get
import kotlin.concurrent.thread

open class UpdaterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
	protected var mView : View? = view
	protected var mContext : Context? = view.context
	protected val mLog : LogUtil = InjektUtil.injekt?.get()!!
	protected val mBus: MyBus = InjektUtil.injekt?.get()!!
	protected val mActivity : MainActivity = InjektUtil.injekt?.get()!!
	protected val mAppState : AppState = InjektUtil.injekt?.get()!!

	open fun bind(adapter : UpdaterAdapter, updates : MergedUpdate) {
		val u : Update = updates.updateList[0]

		//App Name
		mView?.installed_app_name?.text = u.name

		//App Version
		val v = if(u.newVersion == "?" && updates.updateList.size > 1) updates.updateList[1].newVersion else u.newVersion
		mView?.installed_app_version?.text = String.format("%s (%s) -> %s (%s)", u.version, u.versionCode, v, u.newVersionCode)

		//App Icon
		mView?.installed_app_icon?.setImageDrawable(mView?.context?.packageManager?.getApplicationIcon(u.pname))

		//Beta icon
		if (u.isBeta) {
			mView?.installed_app_name?.append(" - β")
		}

		//Install
		mView?.setOnClickListener {
			if (getActionString(u) == mContext?.getString(R.string.action_play)) launchInstall(u) else launchBrowser(u)
		}

		//Source
		mView?.source?.text = getActionString(u)

        //Changelog
		if (u.changeLog.isEmpty()){
			mView?.actionButton?.visibility = View.GONE
		} else {
            mView?.actionButton?.visibility = View.VISIBLE
        }
		mView?.changelog?.text = Html.fromHtml(u.changeLog)
        mView?.actionButton?.setOnClickListener {
            if (mView?.changelog?.visibility == View.VISIBLE) {
                mView?.changelog?.visibility = View.GONE
            } else {
                mView?.changelog?.visibility = View.VISIBLE
            }
        }
		setTopMargin(0)
	}

	private fun launchInstall(u : Update) {
		changeAppInstallStatusAndNotify(u, InstallStatus.STATUS_INSTALLING, 0)
		thread {
			try {
				val data = GooglePlayUtil.getAppDeliveryData(GooglePlayUtil.getApi(mContext), u.pname)

				val id = DownloadUtil.downloadFile(
					mContext,
					data.downloadUrl,
					data.getDownloadAuthCookie(0).name + "=" + data.getDownloadAuthCookie(0).value,
					u.pname + " " + u.newVersionCode
				)

				mAppState.downloadInfo.put(id, DownloadInfo(u.pname, u.newVersionCode, u.newVersion))
				changeAppInstallStatusAndNotify(u, InstallStatus.STATUS_INSTALLING, id)
			} catch (gex: GooglePlayException) {
				SnackBarUtil.make(mActivity, gex.message.toString())
				mLog.log("UpdaterAdapter", gex.toString(), LogMessage.SEVERITY_ERROR)
				changeAppInstallStatusAndNotify(u, InstallStatus.STATUS_INSTALL, 0)
			} catch (e: Exception) {
				SnackBarUtil.make(mActivity, "Error downloading.")
				mLog.log("UpdaterAdapter", e.toString(), LogMessage.SEVERITY_ERROR)
				changeAppInstallStatusAndNotify(u, InstallStatus.STATUS_INSTALL, 0)
			}
		}
	}

	private fun launchBrowser(u : Update) {
		DownloadUtil.launchBrowser(mContext, u.url)
	}

	private fun getActionString(u : Update) : String {
		if (u.url.contains("apkmirror.com")) {
			return mContext?.getString(R.string.action_apkmirror)!!
		} else if (u.url.contains("uptodown.com")) {
			return mContext?.getString(R.string.action_uptodown)!!
		} else if (u.url.contains("apkpure.com")) {
			return mContext?.getString(R.string.action_apkpure)!!
		} else if (u.cookie != null) {
			if (u.installStatus.status == InstallStatus.STATUS_INSTALL) {
				return mContext?.getString(R.string.action_play)!!
			} else if (u.installStatus.status == InstallStatus.STATUS_INSTALLED) {
				return mContext?.getString(R.string.action_installed)!!
			} else if (u.installStatus.status == InstallStatus.STATUS_INSTALLING) {
				return ""
			}
		}
		return "ERROR"
	}

	private fun changeAppInstallStatusAndNotify(app: Update?, status: Int, id: Long) {
		val adapter : UpdaterAdapter = InjektUtil.injekt?.get()!!
		app?.installStatus?.id = id
		app?.installStatus?.status = status
		mView?.post {
			adapter.notifyItemChanged(adapterPosition)
		}
	}

	fun setTopMargin(margin: Int) {
		val params = mView?.layoutParams as ViewGroup.MarginLayoutParams?
		params?.topMargin = PixelConversion.convertDpToPixel(margin.toFloat(), mContext).toInt()
	}
}