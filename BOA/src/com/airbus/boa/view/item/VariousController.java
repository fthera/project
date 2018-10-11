package com.airbus.boa.view.item;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.airbus.boa.entity.article.Various;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;

@ManagedBean(name = VariousController.BEAN_NAME)
@ViewScoped
public class VariousController extends AbstractArticleController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "variousController";

    public VariousController() {
        itemPage = NavigationConstants.VARIOUS_MANAGEMENT_PAGE;
        listPage = NavigationConstants.VARIOUS_LIST_PAGE;
    }
    
    @Override
    protected void initItemWithNew() {
        article = new Various();
    }
    
    @Override
    protected void initItemFromDatabase() {
        article = articleBean.findArticleById(articleId);
        if (!(article instanceof Various)) {
            article = null;
        }
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createVariousTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoVariousTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateVariousTitle");
    }
    
}
