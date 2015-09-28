/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.app.web.base;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.dbflute.Entity;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.hook.AccessContext;
import org.dbflute.optional.OptionalObject;
import org.dbflute.optional.OptionalThing;
import org.docksidestage.app.logic.i18n.I18nDateLogic;
import org.docksidestage.app.web.base.login.HarborLoginAssist;
import org.docksidestage.app.web.base.paging.PagingNavi;
import org.docksidestage.mylasta.action.HarborHtmlPath;
import org.docksidestage.mylasta.action.HarborMessages;
import org.docksidestage.mylasta.action.HarborUserBean;
import org.docksidestage.mylasta.direction.HarborConfig;
import org.lastaflute.db.dbflute.accesscontext.AccessContextArranger;
import org.lastaflute.db.dbflute.accesscontext.AccessContextResource;
import org.lastaflute.web.TypicalAction;
import org.lastaflute.web.callback.ActionRuntime;
import org.lastaflute.web.login.LoginManager;
import org.lastaflute.web.response.ActionResponse;
import org.lastaflute.web.response.render.RenderData;
import org.lastaflute.web.servlet.request.RequestManager;
import org.lastaflute.web.validation.ActionValidator;
import org.lastaflute.web.validation.LaValidatable;

/**
 * @author jflute
 */
public abstract class HarborBaseAction extends TypicalAction // has several interfaces for direct use
        implements LaValidatable<HarborMessages>, HarborHtmlPath {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The application type for HarBoR, e.g. used by access context. */
    protected static final String APP_TYPE = "HBR"; // #change_it_first

    /** The user type for Member, e.g. used by access context. */
    protected static final String USER_TYPE = "M"; // #change_it_first

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private RequestManager requestManager;
    @Resource
    private HarborConfig harborConfig;
    @Resource
    private HarborLoginAssist harborLoginAssist;
    @Resource
    private I18nDateLogic i18nDateLogic;

    // ===================================================================================
    //                                                                               Hook
    //                                                                              ======
    // to suppress unexpected override by sub-class
    // you should remove the 'final' if you need to override this
    @Override
    public final ActionResponse godHandPrologue(ActionRuntime runtime) {
        return super.godHandPrologue(runtime);
    }

    @Override
    public final ActionResponse godHandMonologue(ActionRuntime runtime) {
        return super.godHandMonologue(runtime);
    }

    @Override
    public final void godHandEpilogue(ActionRuntime runtime) {
        super.godHandEpilogue(runtime);
    }

    // #app_customize you can customize the action hook
    @Override
    public ActionResponse hookBefore(ActionRuntime runtime) { // application may override
        return super.hookBefore(runtime);
    }

    @Override
    public void hookFinally(ActionRuntime runtime) {
        if (runtime.isForwardToHtml()) {
            runtime.registerData("headerBean", getUserBean().map(userBean -> {
                return new HarborHeaderBean(userBean);
            }).orElse(HarborHeaderBean.empty()));
        }
        super.hookFinally(runtime);
    }

    // ===================================================================================
    //                                                                      Access Context
    //                                                                      ==============
    @Override
    protected AccessContextArranger newAccessContextArranger() { // for framework
        return resource -> {
            final AccessContext context = new AccessContext();
            context.setAccessLocalDateTimeProvider(() -> currentDateTime());
            context.setAccessUserProvider(() -> buildAccessUserTrace(resource));
            return context;
        };
    }

    private String buildAccessUserTrace(AccessContextResource resource) {
        // #app_customize you can customize the user trace for common column
        final StringBuilder sb = new StringBuilder();
        sb.append(myUserType().map(userType -> userType + ":").orElse(""));
        sb.append(getUserBean().map(bean -> bean.getUserId()).orElseGet(() -> -1));
        sb.append(",").append(myAppType()).append(",").append(resource.getModuleName());
        final String trace = sb.toString();
        final int columnSize = 200;
        return trace.length() > columnSize ? trace.substring(0, columnSize) : trace;
    }

    // ===================================================================================
    //                                                                           User Info
    //                                                                           =========
    @Override
    protected OptionalThing<HarborUserBean> getUserBean() { // to return as concrete class
        return harborLoginAssist.getSessionUserBean(); // #app_customize return empty if login is unused
    }

    @Override
    protected String myAppType() { // for framework
        return APP_TYPE;
    }

    @Override
    protected OptionalThing<String> myUserType() { // for framework
        return OptionalObject.of(USER_TYPE); // #app_customize return empty if login is unused
    }

    @Override
    protected OptionalThing<LoginManager> myLoginManager() {
        return OptionalThing.of(harborLoginAssist);
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    @SuppressWarnings("unchecked")
    @Override
    public ActionValidator<HarborMessages> createValidator() { // for co-variant
        return super.createValidator();
    }

    @Override
    public HarborMessages createMessages() { // application may call
        return new HarborMessages(); // overriding to change return type to concrete-class
    }

    // ===================================================================================
    //                                                                              Paging
    //                                                                              ======
    // #app_customize you can customize the paging navigation logic
    /**
     * Register the paging navigation as page-range.
     * @param data The data object to render the HTML. (NotNull)
     * @param page The selected page as bean of paging result. (NotNull)
     * @param form The form for query string added to link. (NotNull)
     */
    protected void registerPagingNavi(RenderData data, PagingResultBean<? extends Entity> page, Object form) { // application may call
        data.register("pagingNavi", createPagingNavi(page, form));
    }

    protected PagingNavi createPagingNavi(PagingResultBean<? extends Entity> page, Object form) { // application may override
        return new PagingNavi(page, op -> {
            op.rangeSize(harborConfig.getPagingPageRangeSizeAsInteger());
            if (harborConfig.isPagingPageRangeFillLimit()) {
                op.fillLimit();
            }
        } , form);
    }

    /**
     * Get page size (record count of one page) for paging.
     * @return The integer as page size. (NotZero, NotMinus)
     */
    protected int getPagingPageSize() { // application may call
        return harborConfig.getPagingPageSizeAsInteger();
    }

    // ===================================================================================
    //                                                                   Conversion Helper
    //                                                                   =================
    // #app_customize you can customize the conversion logic
    // -----------------------------------------------------
    //                                         to Local Date
    //                                         -------------
    protected OptionalThing<LocalDate> toDate(String exp) { // application may call
        return i18nDateLogic.toDate(exp, myConvZone());
    }

    protected OptionalThing<LocalDateTime> toDateTime(String exp) { // application may call
        return i18nDateLogic.toDateTime(exp, myConvZone());
    }

    // -----------------------------------------------------
    //                                        to String Date
    //                                        --------------
    protected OptionalThing<String> toStringDate(LocalDate date) { // application may call
        return i18nDateLogic.toStringDate(date, myConvZone());
    }

    protected OptionalThing<String> toStringDate(LocalDateTime dateTime) { // application may call
        return i18nDateLogic.toStringDate(dateTime, myConvZone());
    }

    protected OptionalThing<String> toStringDateTime(LocalDateTime dateTime) { // application may call
        return i18nDateLogic.toStringDate(dateTime, myConvZone());
    }

    // -----------------------------------------------------
    //                                   Conversion Resource
    //                                   -------------------
    protected TimeZone myConvZone() {
        return requestManager.getUserTimeZone();
    }

    // ===================================================================================
    //                                                                            Document
    //                                                                            ========
    /**
     * {@inheritDoc} <br>
     * Application Origin Methods:
     * <pre>
     * <span style="font-size: 130%; color: #553000">[Paging]</span>
     * o registerPagingNavi() <span style="color: #3F7E5E">// register paging navigation to HTML</span>
     * o getPagingPageSize() <span style="color: #3F7E5E">// get page size: record count per one page</span>
     * 
     * <span style="font-size: 130%; color: #553000">[Conversion Helper]</span>
     * o toDate(exp) <span style="color: #3F7E5E">// convert expression to local date</span>
     * o toDateTime(exp) <span style="color: #3F7E5E">// convert expression to local date-time</span>
     * o toStringDate(date) <span style="color: #3F7E5E">// convert local date to display expression</span>
     * o toStringDateTime(date) <span style="color: #3F7E5E">// convert local date-time to display expression</span>
     * </pre>
     */
    @Override
    public void document1_CallableSuperMethod() {
        super.document1_CallableSuperMethod();
    }
}