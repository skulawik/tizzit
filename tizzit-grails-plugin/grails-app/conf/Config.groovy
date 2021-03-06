// configuration for plugin testing - will not be included in the plugin zip
// log4j configuration
log4j = {
	appenders {
		console name: 'stdout', layout: pattern(conversionPattern: '%d %-5p [%c] %m%n')
		'null' name: 'stacktrace'
	}

	error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
		  'org.codehaus.groovy.grails.web.pages', //  GSP
		  'org.codehaus.groovy.grails.web.sitemesh', //  layouts
		  'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
		  'org.codehaus.groovy.grails.web.mapping', // URL mapping
		  'org.codehaus.groovy.grails.commons', // core / classloading
		  'org.codehaus.groovy.grails.plugins', // plugins
		  'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
		  'org.springframework',
		  'org.hibernate',
		  'net.sf.ehcache.hibernate'

	warn 'org.mortbay.log'

	debug 'grails.app.controller', 'grails.app.service', 'grails.app.tagLib'

	info 'org.apache',
		 'org.codehaus',
		 'net.sf',
		 'grails',
		 'httpclient'

	root {
		debug 'stdout', 'file'
		additivity = true
	}
} 

grails.rest.injectInto = ["Controller", "Service", "TagLib"]
 

// The following properties have been added by the Upgrade process...
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
