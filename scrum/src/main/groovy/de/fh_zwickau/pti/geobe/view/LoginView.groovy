package de.fh_zwickau.pti.geobe.view

import com.vaadin.annotations.Theme
import com.vaadin.event.ShortcutAction
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.Alignment
import com.vaadin.ui.Component
import com.vaadin.ui.Notification
import com.vaadin.ui.UI
import com.vaadin.ui.themes.ValoTheme
import de.geobe.util.vaadin.VaadinBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.AuthenticationException
import org.vaadin.spring.security.VaadinSecurity

import static VaadinBuilder.C
import static VaadinBuilder.F

/**
 * a simple login page
 * @author georg beier
 */
@SpringUI(path = 'login')
@Theme(ValoTheme.THEME_NAME)
class LoginView extends UI {

    @Autowired
    private VaadinSecurity vaadinSecurity

    private VaadinBuilder loginBuilder
    private widgets = [:]

//    @Autowired
//    private LoginScreen loginScreen

    @Override
    protected void init(VaadinRequest request) {
        setContent(initLayout())
    }

    private Component initLayout() {
        loginBuilder = new VaadinBuilder()
        Component root
        root = loginBuilder."$C.vlayout"('rootLayout', [uikey    : 'root',
                                                        alignment: Alignment.TOP_CENTER]) {
            "$C.vlayout"([uikey    : 'loginLayout', sizeUndefined: null,
                          alignment: Alignment.MIDDLE_CENTER]) {
                "$F.label"([uikey    : 'loginFailedLabel', styleName: ValoTheme.LABEL_FAILURE,
                            visible  : false, sizeUndefined: null,
                            alignment: Alignment.BOTTOM_CENTER])
                "$F.label"([uikey    : 'loggedOutLabel', styleName: ValoTheme.LABEL_SUCCESS,
                            visible  : false, sizeUndefined: null,
                            alignment: Alignment.BOTTOM_CENTER])
                "$C.formlayout"([uikey    : 'form', sizeUndefined: null,
                                 alignment: Alignment.TOP_CENTER]) {
                    "$F.text"('Username', [uikey: 'userName'])
                    "$F.password"('Password', [uikey: 'password'])
                    "$F.checkbox"('Remember me', [uikey: 'rememberMe'])
                    "$F.button"('Login', [uikey         : 'loginButton',
                                          styleName     : ValoTheme.BUTTON_PRIMARY,
                                          disableOnClick: true,
                                          clickShortcut : ShortcutAction.KeyCode.ENTER,
                                          clickListener : { login(it) }])
                }
            }
        }
        widgets = loginBuilder.uiComponents
        root
    }

    private void login(def event) {
        try {
            vaadinSecurity.login(widgets['userName'].value,
                    widgets['password'].value, widgets['rememberMe'].value);
            Notification.show('Login successful', "user ${widgets['userName'].value}",
                    Notification.Type.HUMANIZED_MESSAGE)
        } catch (AuthenticationException ex) {
            widgets['userName'].focus();
            widgets['userName'].selectAll();
            widgets['password'].setValue("");
            widgets['loginFailedLabel'].setValue(String.format("Login failed: %s", ex.getMessage()));
            widgets['loginFailedLabel'].setVisible(true);
            if (widgets['loggedOutLabel'] != null) {
                widgets['loggedOutLabel'].setVisible(false);
            }
        } catch (Exception ex) {
            Notification.show("An unexpected error occurred", ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE);
            log.error("Unexpected error while logging in", ex);
        } finally {
            widgets['loginButton'].enabled = true
        }
    }
}
