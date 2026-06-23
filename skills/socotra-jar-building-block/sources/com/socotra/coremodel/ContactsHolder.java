package com.socotra.coremodel;

import com.socotra.coremodel.interfaces.Contact;
import com.socotra.deployment.DataFetcher;
import com.socotra.deployment.DataFetcherFactory;
import com.socotra.platform.tools.ULID;
import java.util.*;
import java.util.function.Supplier;

public interface ContactsHolder {
  Map<String, ContactSlot> contactSlots();

  Collection<ContactRoles> contacts();

  default Collection<String> validateContacts() {
    return validateContacts(DataFetcherFactory::get);
  }

  default Collection<String> validateContacts(Supplier<DataFetcher> dataFetcherSupplier) {
    Collection<String> errors = new ArrayList<>();
    Map<String, Collection<com.socotra.coremodel.interfaces.Contact<?>>> roleContacts =
        new HashMap<>();
    Map<String, Collection<ULID>> roleContactLocators = new HashMap<>();
    Collection<ContactRoles> contacts = contacts() == null ? List.of() : contacts();
    contacts.forEach(
        c -> {
          if (c.roles().isEmpty()) {
            errors.add("contact " + c.contactLocator() + " has no assigned role");
          } else {
            c.roles()
                .forEach(
                    r ->
                        roleContactLocators
                            .computeIfAbsent(r, k -> new ArrayList<>())
                            .add(c.contactLocator()));
          }
        });
    roleContactLocators.forEach(
        (role, locators) ->
            locators.forEach(
                locator -> {
                  com.socotra.coremodel.interfaces.Contact<?> contact =
                      dataFetcherSupplier.get().getContact(locator);
                  if (contact == null) {
                    errors.add("no such contact " + locator);
                  } else if (contact.contactState() != ContactState.validated) {
                    errors.add("contact " + locator + " is not validated");
                  } else {
                    ContactSlot slot = contactSlots().get(role);
                    if (slot == null) {
                      errors.add("'" + role + "' is not an acceptable role for contact " + locator);
                    } else {
                      if (!slot.types().contains(contact.type())) {
                        errors.add(
                            "contact "
                                + locator
                                + " has unacceptable type '"
                                + contact.type()
                                + "' for role '"
                                + role
                                + "'");
                      } else {
                        roleContacts.computeIfAbsent(role, k -> new ArrayList<>()).add(contact);
                      }
                    }
                  }
                }));
    contactSlots()
        .forEach(
            (role, slot) -> {
              Collection<Contact<?>> c = roleContacts.get(role);
              int count = c == null ? 0 : c.size();
              if (count < slot.minSize()) {
                errors.add(
                    "require at least "
                        + slot.minSize()
                        + " valid contacts for role '"
                        + role
                        + "', got "
                        + count);
              }
              slot.maxSize()
                  .filter(max -> max < count)
                  .ifPresent(
                      maxSize ->
                          errors.add(
                              "require no more than "
                                  + maxSize
                                  + " valid contacts for role '"
                                  + role
                                  + "', got "
                                  + count));
            });
    return errors;
  }
}
