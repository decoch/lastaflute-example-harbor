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
package org.docksidestage.app.web.lidoisle.member;

import javax.annotation.Resource;

import org.docksidestage.app.web.base.HarborBaseAction;
import org.docksidestage.dbflute.exbhv.MemberBhv;

/**
 * @author jflute
 */
public class MemberAddAction extends HarborBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MemberBhv memberBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======

    // TODO (s.tadokoro) implement
    //    @Execute
    //    public HtmlResponse index() {
    //        return asHtml(path_Member_MemberAddJsp).useForm(MemberAddForm.class);
    //    }
    //
    //    @Execute
    //    public HtmlResponse register(MemberAddForm form) {
    //        validate(form, messages -> {} , () -> {
    //            return asHtml(path_Member_MemberAddJsp);
    //        });
    //        Member member = new Member();
    //        member.setMemberName(form.memberName);
    //        member.setMemberAccount(form.memberAccount);
    //        member.setBirthdate(form.birthdate);
    //        member.setMemberStatusCodeAsMemberStatus(form.memberStatus);
    //        if (member.isMemberStatusCodeFormalized()) {
    //            member.setFormalizedDatetime(currentDateTime());
    //        }
    //        memberBhv.insert(member);
    //        return redirect(MemberListAction.class);
    //    }
}
