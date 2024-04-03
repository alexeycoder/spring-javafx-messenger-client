package edu.alexey.messengerclient.viewmodel;

import java.util.List;

import edu.alexey.messengerclient.dto.ContactDto;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModelConsumer;

public interface FindContactViewModelConsumer extends ViewModelConsumer<Void, List<ContactDto>> {
}
