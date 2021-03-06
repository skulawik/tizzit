/**
 * Copyright (c) 2009 Juwi MacMillan Group GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.juwimm.cms.cocoon.acting;

import org.apache.avalon.framework.thread.SingleThreaded;

/**
 *
 * @author toerberj
 *
 * @see de.juwimm.cms.cocoon.acting.GenericAction
 *
 * AbstractAction implements Action, Component, LogEnabled
 * AbstractConfigurableAction implements Action, Component, LogEnabled, Configurable
 *
 * we already have some Actions which implement Interface SingleThreaded, so we assume we need a GenericAction which also implements SingleThreaded;
 * we also assume that single threaded is enough at the moment;
 *
 * @deprecated Use {@link org.tizzit.cocoon.generic.acting.GenericActionSingleThreaded} instead!
 */
@Deprecated
public class GenericActionSingleThreaded extends org.tizzit.cocoon.generic.acting.GenericActionSingleThreaded implements SingleThreaded {
}