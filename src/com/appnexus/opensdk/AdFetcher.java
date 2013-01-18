package com.appnexus.opensdk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdFetcher {
	private ScheduledExecutorService tasker;
	private AdView owner;
	private int period = -1;
	private boolean autoRefresh;

	public AdFetcher(AdView owner) {
		this.owner = owner;
	}

	protected void setPeriod(int period) {
		this.period = period;
		// Restart with new period TODO this dumps the current ad, which is bad
		// if server response changes the period
		if (tasker != null) {
			stop();
			start();
		}
	}

	protected int getPeriod() {
		return period;
	}

	protected void stop() {
		if (tasker == null)
			return; // You can't stop the signal Mal
		tasker.shutdown();
		tasker = null;
	}

	protected void start() {
		if (tasker != null) {
			Clog.d("OPENSDK",
					"AdFetcher requested to start, but tasker already instantiated");
			return;
		}
		makeTasker();
	}

	private void makeTasker() {
		// Start a Scheduler to execute recurring tasks
		tasker = Executors.newScheduledThreadPool(1);

		// Get the period from the settings
		int msPeriod = period == -1 ? 60 * 1000 : period;

		if (!getAutoRefresh()) {
			Clog.d("OPENSDK", "AdFetcher started in single-use mode");
			// Request an ad once
			tasker.schedule(new Runnable() {
				@Override
				public void run() {
					Clog.v("OPENSDK", "AdRequest thread started.");
					new AdRequest(owner).execute();
				}
			}, 0, TimeUnit.SECONDS);
		} else {
			Clog.d("OPENSDK", "AdFetcher started in autorefresh-mode");
			// Start recurring ad requests
			tasker.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					try {
						Clog.v("OPENSDK", "AdRequest thread started.");
						new AdRequest(owner).execute();
					} catch (Exception e) {
						e.printStackTrace();
						Clog.e("OPENSDK", "Fuuuuuuuuuck");
					}
				}
			}, 0, msPeriod, TimeUnit.MILLISECONDS);
		}
	}

	protected boolean getAutoRefresh() {
		return autoRefresh;
	}

	protected void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
		// Restart with new autorefresh setting
		if (tasker != null) {
			stop();
			start();
		}
	}
}
