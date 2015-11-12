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
package org.docksidestage.app.web.lidoisle.product;

import javax.annotation.Resource;

import org.dbflute.cbean.result.PagingResultBean;
import org.docksidestage.app.web.base.HarborBaseAction;
import org.docksidestage.dbflute.exbhv.ProductBhv;
import org.docksidestage.dbflute.exbhv.ProductStatusBhv;
import org.docksidestage.dbflute.exentity.Product;
import org.lastaflute.web.login.AllowAnyoneAccess;

/**
 * @author jflute
 */
@AllowAnyoneAccess
public class ProductListAction extends HarborBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private ProductBhv productBhv;
    @Resource
    private ProductStatusBhv productStatusBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======

    // TODO (s.tadokoro) implement
    //    @Execute
    //    public HtmlResponse index(OptionalThing<Integer> pageNumber, ProductSearchForm form) {
    //        validate(form, messages -> {} , () -> {
    //            return asHtml(path_Product_ProductListHtml);
    //        });
    //        PagingResultBean<Product> page = selectProductPage(pageNumber.orElse(1), form);
    //        List<ProductSearchRowBean> beans = page.mappingList(product -> {
    //            return mappingToBean(product);
    //        });
    //        return asHtml(path_Product_ProductListHtml).renderWith(data -> {
    //            data.register("beans", beans);
    //            registerPagingNavi(data, page, form);
    //        });
    //    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private PagingResultBean<Product> selectProductPage(int pageNumber, ProductSearchForm form) {
        verifyParameterTrue("The pageNumber should be positive number: " + pageNumber, pageNumber > 0);
        return productBhv.selectPage(cb -> {
            cb.ignoreNullOrEmptyQuery();
            cb.setupSelect_ProductStatus();
            cb.setupSelect_ProductCategory();
            cb.specify().derivedPurchase().count(purchaseCB -> {
                purchaseCB.specify().columnPurchaseId();
            } , Product.ALIAS_purchaseCount);
            cb.query().setProductName_LikeSearch(form.productName, op -> op.likeContain());
            final String purchaseMemberName = form.purchaseMemberName;
            if (isNotEmpty(purchaseMemberName)) {
                cb.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().queryMember().setMemberName_LikeSearch(purchaseMemberName, op -> op.likeContain());
                });
            }
            cb.query().setProductStatusCode_Equal_AsProductStatus(form.productStatus);
            cb.query().addOrderBy_ProductName_Asc();
            cb.query().addOrderBy_ProductId_Asc();
            cb.paging(getPagingPageSize(), pageNumber);
        });
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private ProductSearchRowBean mappingToBean(Product product) {
        ProductSearchRowBean bean = new ProductSearchRowBean();
        bean.productId = product.getProductId();
        bean.productName = product.getProductName();
        bean.regularPrice = product.getRegularPrice();
        bean.registerDatetime = product.getRegisterDatetime();
        product.getProductStatus().alwaysPresent(status -> {
            bean.productStatusName = status.getProductStatusName();
        });
        product.getProductCategory().alwaysPresent(category -> {
            bean.productCategoryName = category.getProductCategoryName();
        });
        return bean;
    }
}
