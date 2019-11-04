package eu.supersede.mdm.storage;

import eu.supersede.mdm.storage.db.mongo.repositories.*;
import eu.supersede.mdm.storage.resources.WrapperResource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

//For injection annotation @Inject
public class MyApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(UserRepository.class).to(UserRepository.class);
        bind(GlobalGraphRepository.class).to(GlobalGraphRepository.class);
        bind(WrapperRepository.class).to(WrapperRepository.class);
        bind(DataSourceRepository.class).to(DataSourceRepository.class);
        bind(LAVMappingRepository.class).to(LAVMappingRepository.class);
    }
}
