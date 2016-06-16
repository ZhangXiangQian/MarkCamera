package com.bankeys.camera;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;

/**
 * 自动调整view宽高,使之成为正方行.<br>
 * veiw.getViewTreeObserver().addOnPreDrawListener(new AutoSizeListener(view))
 * @author bankeys
 * 
 */
public class AutoSizeListener implements OnPreDrawListener {
	private View view;
	boolean hasDraw = false;
	private float x = 1.0F;

	public AutoSizeListener(View V) {
		view = V;
	}

	public AutoSizeListener(View V, float x) {
		view = V;
		this.x = x;
	}

	@Override
	public boolean onPreDraw() {
		if (hasDraw == false) {
			int h = view.getHeight();
			int w = view.getWidth();

			if (w != 0 && h != 0) {
				hasDraw = true;
				zoom(w, h);
			}
		}
		return true;
	}

	private void zoom(int w, int h) {
		LayoutParams viewParam = view.getLayoutParams();

		if (w > 2 * h) {
			viewParam.width = (int) (h * x);
			viewParam.height = h;
		} else if (h > 2 * w) {
			viewParam.height = w;
			viewParam.width = (int) (w / x);
		} else if (w > h) {
			if (x != 1F) {
				viewParam.width = w;
				viewParam.height = (int) (w / x);
			} else {
				viewParam.height = h;
				viewParam.width = (int) (h / x);
			}
		} else if (h > w) {
			viewParam.width = w;
			viewParam.height = (int) (w / x);
		}

		view.setLayoutParams(viewParam);
	}
}
