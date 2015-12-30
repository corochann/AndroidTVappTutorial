package com.corochann.androidtvapptutorial.model;

import android.support.v17.leanback.widget.HeaderItem;

import com.corochann.androidtvapptutorial.ui.presenter.IconHeaderItemPresenter;

/**
 * Subclass of {@link HeaderItem} to hold icon resource id
 * to show icon on header with {@link IconHeaderItemPresenter}
 */
public class IconHeaderItem extends HeaderItem {


    private static final String TAG = IconHeaderItem.class.getSimpleName();
    public static final int ICON_NONE = -1;

    /** Hold an icon resource id */
    private int mIconResId = ICON_NONE;

    public IconHeaderItem(long id, String name, int iconResId) {
        super(id, name);
        mIconResId = iconResId;
    }

    public IconHeaderItem(long id, String name) {
        this(id, name, ICON_NONE);
    }

    public IconHeaderItem(String name) {
        super(name);
    }

    public int getIconResId() {
        return mIconResId;
    }

    public void setIconResId(int iconResId) {
        this.mIconResId = iconResId;
    }
}