package com.airbus.boa.view.item;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.airbus.boa.entity.article.Cabinet;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;

@ManagedBean(name = CabinetController.BEAN_NAME)
@ViewScoped
public class CabinetController extends AbstractArticleController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "cabinetController";

    public CabinetController() {
        itemPage = NavigationConstants.CABINET_MANAGEMENT_PAGE;
        listPage = NavigationConstants.CABINET_LIST_PAGE;
    }
    
    @Override
    protected void initItemWithNew() {
        article = new Cabinet();
    }
    
    @Override
    protected void initItemFromDatabase() {
        article = articleBean.findArticleById(articleId);
        if (!(article instanceof Cabinet)) {
            article = null;
        }
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createCabinetTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoCabinetTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateCabinetTitle");
    }
    
}
