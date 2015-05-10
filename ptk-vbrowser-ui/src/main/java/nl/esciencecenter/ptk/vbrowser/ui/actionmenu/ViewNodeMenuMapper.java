package nl.esciencecenter.ptk.vbrowser.ui.actionmenu;

import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.viewers.menu.MenuMapping;
import nl.esciencecenter.ptk.vbrowser.viewers.menu.MenuMappingMatcher;

public class ViewNodeMenuMapper implements MenuMappingMatcher {

    protected ViewNode viewNode;

    public ViewNodeMenuMapper(ViewNode viewNode) {
        this.viewNode = viewNode;
    }

    @Override
    public boolean matches(MenuMapping menuMap) {

        return menuMap.matches(viewNode.getResourceType(), viewNode.getVRL().getScheme(),
                viewNode.getResourceStatus(), viewNode.getMimeType());
    }

}
