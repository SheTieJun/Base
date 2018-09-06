package me.shetj.base.qmui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * the container activity for {@link QMUIFragment}.
 * Created by cgspine on 15/9/14.
 */
public abstract class QMUIFragmentActivity extends RxAppCompatActivity {
    private static final String TAG = "QMUIFragmentActivity";
    private QMUIWindowInsetLayout mFragmentContainer;

    @SuppressWarnings("SameReturnValue")
    protected abstract int getContextViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        mFragmentContainer = new QMUIWindowInsetLayout(this);
        mFragmentContainer.setId(getContextViewId());
        setContentView(mFragmentContainer);
    }

    public FrameLayout getFragmentContainer() {
        return mFragmentContainer;
    }

    @Override
    public void onBackPressed() {
        QMUIFragment fragment = getCurrentFragment();
        if (fragment != null) {
            fragment.popBackStack();
        }
    }

    /**
     * get the current Fragment.
     */
    public QMUIFragment getCurrentFragment() {
        return ( QMUIFragment) getSupportFragmentManager().findFragmentById(getContextViewId());
    }

    /**
     * start a new fragment and then destroy current fragment.
     * assume there is a fragment stack(A->B->C), and you use this method to start a new
     * fragment D and destroy fragment C. Now you are in fragment D, if you want call
     * {@link #popBackStack()} to back to B, what the animation should be? Sometimes we hope run
     * animation generated by transition B->C, but sometimes we hope run animation generated by
     * transition C->D. this why second parameter exists.
     *
     * @param fragment                      new fragment to start
     * @param useNewTransitionConfigWhenPop if true, use animation generated by transition C->D,
     *                                      else, use animation generated by transition B->C
     */
    public int startFragmentAndDestroyCurrent(final QMUIFragment fragment, final boolean useNewTransitionConfigWhenPop) {
        final QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(transitionConfig.enter, transitionConfig.exit,
                        transitionConfig.popenter, transitionConfig.popout)
                .replace(getContextViewId(), fragment, tagName);
        int index = transaction.commit();
        Utils.findAndModifyOpInBackStackRecord(fragmentManager, -1, new Utils.OpHandler() {
            @Override
            public boolean handle(Object op) {
                Field cmdField = null;
                try {
                    cmdField = op.getClass().getDeclaredField("cmd");
                    cmdField.setAccessible(true);
                    int cmd = (int) cmdField.get(op);
                    if (cmd == 1) {
                        if (useNewTransitionConfigWhenPop) {
                            Field popEnterAnimField = op.getClass().getDeclaredField("popEnterAnim");
                            popEnterAnimField.setAccessible(true);
                            popEnterAnimField.set(op, transitionConfig.popenter);

                            Field popExitAnimField = op.getClass().getDeclaredField("popExitAnim");
                            popExitAnimField.setAccessible(true);
                            popExitAnimField.set(op, transitionConfig.popout);
                        }

                        Field oldFragmentField = op.getClass().getDeclaredField("fragment");
                        oldFragmentField.setAccessible(true);
                        Object fragmentObj = oldFragmentField.get(op);
                        oldFragmentField.set(op, fragment);
                        Field backStackNestField = Fragment.class.getDeclaredField("mBackStackNesting");
                        backStackNestField.setAccessible(true);
                        int oldFragmentBackStackNest = (int) backStackNestField.get(fragmentObj);
                        backStackNestField.set(fragment, oldFragmentBackStackNest);
                        backStackNestField.set(fragmentObj, --oldFragmentBackStackNest);
                        return true;
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        return index;
    }

    public int startFragment( QMUIFragment fragment) {
        Timber.i ( "startFragment");
        QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
        String tagName = fragment.getClass().getSimpleName();
        return getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(transitionConfig.enter, transitionConfig.exit, transitionConfig.popenter, transitionConfig.popout)
                .replace(getContextViewId(), fragment, tagName)
                .addToBackStack(tagName)
                .commit();
    }

    /**
     * Exit the current Fragment。
     */
    public void popBackStack() {
        Timber.i( "popBackStack: getSupportFragmentManager().getBackStackEntryCount() = %s", getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            QMUIFragment fragment = getCurrentFragment();
            if (fragment == null) {
                finish();
                return;
            }
            QMUIFragment.TransitionConfig transitionConfig = fragment.onFetchTransitionConfig();
            Object toExec = fragment.onLastFragmentFinish();
            if (toExec != null) {
                if (toExec instanceof QMUIFragment) {
                    QMUIFragment mFragment = ( QMUIFragment) toExec;
                    startFragment(mFragment);
                } else if (toExec instanceof Intent) {
                    Intent intent = (Intent) toExec;
                    finish();
                    startActivity(intent);
                    overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
                } else {
                    throw new Error("can not handle the result in onLastFragmentFinish");
                }
            } else {
                finish();
                overridePendingTransition(transitionConfig.popenter, transitionConfig.popout);
            }
        } else {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    /**
     * pop back to a clazz type fragment
     * <p>
     * Assuming there is a back stack: Home -> List -> Detail. Perform popBackStack(Home.class),
     * Home is the current fragment
     * <p>
     * if the clazz type fragment doest not exist in back stack, this method is Equivalent
     * to popBackStack()
     *
     * @param clazz the type of fragment
     */
    public void popBackStack(Class<? extends QMUIFragment> clazz) {
        getSupportFragmentManager().popBackStack(clazz.getSimpleName(), 0);
    }

    /**
     * pop back to a non-clazz type Fragment
     *
     * @param clazz the type of fragment
     */
    public void popBackStackInclusive(Class<? extends QMUIFragment> clazz) {
        getSupportFragmentManager().popBackStack(clazz.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}