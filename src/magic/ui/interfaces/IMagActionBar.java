package magic.ui.interfaces;

import java.util.List;

import magic.ui.screen.widget.MenuButton;

public interface IMagActionBar {
    MenuButton getLeftAction();
    MenuButton getRightAction();
    List<MenuButton> getMiddleActions();
}
