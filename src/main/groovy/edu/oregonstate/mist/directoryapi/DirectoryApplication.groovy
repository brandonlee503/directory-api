package edu.oregonstate.mist.directoryapi

import edu.oregonstate.mist.api.BuildInfoManager
import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.InfoResource
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.BasicAuthenticator
import edu.oregonstate.mist.api.jsonapi.GenericExceptionMapper
import edu.oregonstate.mist.api.jsonapi.IOExceptionMapper

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.auth.AuthFactory
import io.dropwizard.auth.basic.BasicAuthFactory

/**
 * Main application class.
 */
class DirectoryApplication extends Application<DirectoryApplicationConfiguration> {
    /**
     * Initializes application bootstrap.
     *
     * @param bootstrap
     */
    @Override
    public void initialize(Bootstrap<DirectoryApplicationConfiguration> bootstrap) {}

    /**
     * Registers lifecycle managers and Jersey exception mappers
     *
     * @param environment
     * @param buildInfoManager
     */
    protected void registerAppManagerLogic(Environment environment,
                                           BuildInfoManager buildInfoManager) {

        environment.lifecycle().manage(buildInfoManager)

        environment.jersey().register(new IOExceptionMapper())
        environment.jersey().register(new GenericExceptionMapper())
    }

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(DirectoryApplicationConfiguration configuration, Environment environment) {
        Resource.loadProperties()
        BuildInfoManager buildInfoManager = new BuildInfoManager()
        registerAppManagerLogic(environment, buildInfoManager)

        final DirectoryEntityDAO DIRECTORYENTITYDAO = new DirectoryEntityDAO(
                configuration.getLdapConfiguration()
        )

        environment.healthChecks().register('LDAP', new LDAPHealthCheck(DIRECTORYENTITYDAO))

        environment.jersey().register(new DirectoryEntityResource(DIRECTORYENTITYDAO))
        environment.jersey().register(new InfoResource(buildInfoManager.getInfo()))
        environment.jersey().register(
                AuthFactory.binder(
                        new BasicAuthFactory<AuthenticatedUser>(
                                new BasicAuthenticator(configuration.getCredentialsList()),
                                'DirectoryApplication',
                                AuthenticatedUser.class)))
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new DirectoryApplication().run(arguments)
    }
}
