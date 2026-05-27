package ru.antonov.trainticketservice.ticket.query.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.antonov.trainticketservice.common.exception.EntityNotFoundException;
import ru.antonov.trainticketservice.common.exception.ErrorCode;
import ru.antonov.trainticketservice.common.exception.ResourceAccessDeniedException;
import ru.antonov.trainticketservice.ticket.query.dto.DtoFactory;
import ru.antonov.trainticketservice.ticket.query.dto.TicketDto;
import ru.antonov.trainticketservice.ticket.query.dto.TicketShortDto;
import ru.antonov.trainticketservice.ticket.query.entity.TicketView;
import ru.antonov.trainticketservice.ticket.query.repository.TicketViewRepository;
import ru.antonov.trainticketservice.user.entity.Role;
import ru.antonov.trainticketservice.user.entity.User;
import ru.antonov.trainticketservice.user.service.UserService;

import java.util.List;

import java.util.UUID;

/**
 * Query-side сервис для чтения проекций билетов с проверкой прав доступа.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TicketQueryService {
    private final TicketViewRepository viewRepository;
    private final UserService userService;

    /**
     * Возвращает историю билетов пользователя, если principal является этим пользователем или администратором.
     *
     * @param targetId пользователь, чьи билеты запрашиваются
     * @param principal авторизованный пользователь
     * @return краткие DTO билетов из read model
     */
    public List<TicketShortDto> findTicketsByUserId(
            UUID targetId, User principal
    ) {
        User target = userService.findById(targetId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Данного пользователя не существует",
                        String.format("Ошибка при поиске истории бронирований. Пользователя %s не существует",
                                targetId),
                        ErrorCode.ENTITY_NOT_FOUND
                )
        );

        validateAccessToQueryUserTicketHistory(target, principal);

        List<TicketView> entities = viewRepository.findAllByOwnerId(targetId);

        return entities.stream()
                .map(DtoFactory::makeTicketShortDto)
                .toList();
    }

    /**
     * Возвращает полную информацию о билете, если principal имеет к нему доступ.
     *
     * @param ticketId идентификатор билета
     * @param principal авторизованный пользователь
     * @return полный DTO билета
     */
    public TicketDto findTicketById(UUID ticketId, User principal) {
        TicketView entity = viewRepository.findById(ticketId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Данного билета не существует",
                        String.format("Ошибка при поиске билета. Билета %s не существует", ticketId ),
                        ErrorCode.ENTITY_NOT_FOUND
                )
        );

        validateAccessToTicket(entity, principal);

        return DtoFactory.makeTicketDto(entity);
    }

    /**
     * Возвращает билеты для сегмента рейса.
     *
     * @param cruiseId идентификатор рейса
     * @param stopId идентификатор остановки, используемый в запросе репозитория
     * @return краткие DTO билетов
     */
    public List<TicketShortDto> findAllByCruiseIdAndStopId(UUID cruiseId, UUID stopId){
        return viewRepository.findAllByCruiseIdAndStopId(cruiseId, stopId).stream()
                .map(DtoFactory::makeTicketShortDto)
                .toList();
    }

    private void validateAccessToTicket(TicketView view, User principal){
        if(principal.getRole() != Role.ADMIN && !principal.getId().equals(view.getOwnerId())){
            throw new ResourceAccessDeniedException(
                    "Ошибка. У вас нет прав на просмотр информации об этом билете",
                    String.format("ResourceAccessDeniedException при поиске информации о билете %s пользователем %s",
                            view.getTicketId(), principal.getId()),
                    ErrorCode.FORBIDDEN
            );
        }
    }

    private void validateAccessToQueryUserTicketHistory(User target, User principal){
        if(principal.getRole() != Role.ADMIN && !principal.equals(target)){
            throw new ResourceAccessDeniedException(
                    "Ошибка. У вас нет прав на просмотр истории бронирований этого пользователя",
                    String.format("ResourceAccessDeniedException при поиске истории бронирований:" +
                            " target_id = %s, target_role = %s, principal_id = %s, principal_role = %s",
                            target.getId(), target.getRole(), principal.getId(), principal.getRole()),
                    ErrorCode.FORBIDDEN
            );
        }
    }

}
