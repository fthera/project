package com.airbus.boa.view.item;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;

@ManagedBean(name = SwitchController.BEAN_NAME)
@ViewScoped
public class SwitchController extends AbstractArticleController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "switchController";

    public SwitchController() {
        itemPage = NavigationConstants.SWITCH_MANAGEMENT_PAGE;
        listPage = NavigationConstants.SWITCH_LIST_PAGE;
    }
    
    @Override
    protected void initItemWithNew() {
        article = new Switch();
    }
    
    @Override
    protected void initItemFromDatabase() {
        article = articleBean.findArticleById(articleId);
        if (!(article instanceof Switch)) {
            article = null;
        }
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createSwitchTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoSwitchTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateSwitchTitle");
    }
    
}
