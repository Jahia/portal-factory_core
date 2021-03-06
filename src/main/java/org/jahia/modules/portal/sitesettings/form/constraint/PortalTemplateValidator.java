/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.portal.sitesettings.form.constraint;

import java.util.List;
import javax.jcr.RepositoryException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.portal.service.PortalService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kevan on 03/02/14.
 */
public class PortalTemplateValidator implements ConstraintValidator<PortalTemplate, String> {
	private static final Logger logger = LoggerFactory.getLogger(PortalTemplateValidator.class);

	private PortalService portalService;

	@Override
	public void initialize(PortalTemplate portalTemplate) {
		//TODO Autowired not working in constraint validator, need to investigate why.
		portalService = (PortalService) SpringContextSingleton.getBean("portalService");
	}

	@Override
	public boolean isValid(String portalTemplatePath, ConstraintValidatorContext constraintValidatorContext) {
		if(StringUtils.isEmpty(portalTemplatePath))
			return true;

		try {
			JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
			JCRNodeWrapper portalTabTemplate = portalService.getPortalTabTemplate(portalTemplatePath, session);
			if(portalTabTemplate != null){
				return true;
			}
		} catch (RepositoryException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
}
