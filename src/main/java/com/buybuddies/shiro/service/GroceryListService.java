package com.buybuddies.shiro.service;

import com.buybuddies.shiro.dto.GroceryListDTO;
import com.buybuddies.shiro.entity.GroceryList;
import com.buybuddies.shiro.entity.User;
import com.buybuddies.shiro.repository.GroceryListRepository;
import com.buybuddies.shiro.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GroceryListService {
    private final GroceryListRepository groceryListRepository;
    private final UserRepository userRepository;


    public GroceryListDTO createGroceryList(GroceryListDTO groceryListDTO) {
        User owner = userRepository.findById(groceryListDTO.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        GroceryList groceryList = new GroceryList();
        groceryList.setName(groceryListDTO.getName());
        groceryList.setDescription(groceryListDTO.getDescription());
        groceryList.setOwner(owner);

        if (groceryListDTO.getMemberIds() != null && !groceryListDTO.getMemberIds().isEmpty()) {
            Set<User> members = userRepository.findAllById(groceryListDTO.getMemberIds())
                    .stream()
                    .collect(Collectors.toSet());
            groceryList.setMembers(members);
        }

        groceryList = groceryListRepository.save(groceryList);
        return convertToDTO(groceryList);
    }

    public GroceryListDTO getGroceryList(Long id) {
        GroceryList groceryList = groceryListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grocery list not found"));
        return convertToDTO(groceryList);
    }

    public List<GroceryListDTO> getGroceryListsByUser(Long userId) {
        return groceryListRepository.findByOwnerIdOrMembersId(userId, userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroceryListDTO updateGroceryList(Long id, GroceryListDTO dto) {
        GroceryList groceryList = groceryListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grocery list not found"));

        groceryList.setName(dto.getName());
        groceryList.setDescription(dto.getDescription());

        if (dto.getOwnerId() != null && !dto.getOwnerId().equals(groceryList.getOwner().getId())) {
            User newOwner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("New owner not found"));
            groceryList.setOwner(newOwner);
        }

        if (dto.getMemberIds() != null) {
            Set<User> members = userRepository.findAllById(dto.getMemberIds())
                    .stream()
                    .collect(Collectors.toSet());
            groceryList.setMembers(members);
        }

        groceryList = groceryListRepository.save(groceryList);
        return convertToDTO(groceryList);
    }

    @Transactional
    public GroceryListDTO addMember(Long listId, Long userId) {
        GroceryList groceryList = groceryListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("Grocery list not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        groceryList.getMembers().add(user);
        groceryList = groceryListRepository.save(groceryList);
        return convertToDTO(groceryList);
    }

    @Transactional
    public GroceryListDTO removeMember(Long listId, Long userId) {
        GroceryList groceryList = groceryListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("Grocery list not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        groceryList.getMembers().remove(user);
        groceryList = groceryListRepository.save(groceryList);
        return convertToDTO(groceryList);
    }

    @Transactional
    public void deleteGroceryList(Long id) {
        if (!groceryListRepository.existsById(id)) {
            throw new RuntimeException("Grocery list not found");
        }
        groceryListRepository.deleteById(id);
    }

    private GroceryListDTO convertToDTO(GroceryList groceryList) {
        GroceryListDTO dto = new GroceryListDTO();
        dto.setId(groceryList.getId());
        dto.setName(groceryList.getName());
        dto.setDescription(groceryList.getDescription());
        dto.setOwnerId(groceryList.getOwner().getId());
        dto.setOwnerName(groceryList.getOwner().getName());

        dto.setMemberIds(groceryList.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));

        dto.setMemberNames(groceryList.getMembers().stream()
                .map(User::getName)
                .collect(Collectors.toSet()));

        return dto;
    }
}