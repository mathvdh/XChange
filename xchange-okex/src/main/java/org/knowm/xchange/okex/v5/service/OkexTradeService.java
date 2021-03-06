package org.knowm.xchange.okex.v5.service;

import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.okex.v5.OkexAdapters;
import org.knowm.xchange.okex.v5.OkexExchange;
import org.knowm.xchange.okex.v5.dto.trade.OkexCancelOrderRequest;
import org.knowm.xchange.okex.v5.dto.trade.OkexTradeParams;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/** Author: Max Gao (gaamox@tutanota.com) Created: 08-06-2021 */
public class OkexTradeService extends OkexTradeServiceRaw implements TradeService {
  public OkexTradeService(OkexExchange exchange, ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return OkexAdapters.adaptOpenOrders(
        getOkexPendingOrder(null, null, null, null, null, null, null, null).getData());
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException, FundsExceededException {
    return placeOkexOrder(OkexAdapters.adaptOrder(limitOrder)).getData().get(0).getOrderId();
  }

  public List<String> placeLimitOrder(List<LimitOrder> limitOrders)
      throws IOException, FundsExceededException {
    return placeOkexOrder(
            limitOrders.stream()
                .map(order -> OkexAdapters.adaptOrder(order))
                .collect(Collectors.toList()))
        .getData()
        .stream()
        .map(result -> result.getOrderId())
        .collect(Collectors.toList());
  }

  @Override
  public String changeOrder(LimitOrder limitOrder) throws IOException, FundsExceededException {
    return amendOkexOrder(OkexAdapters.adaptAmendOrder(limitOrder)).getData().get(0).getOrderId();
  }

  public List<String> changeOrder(List<LimitOrder> limitOrders)
      throws IOException, FundsExceededException {
    return amendOkexOrder(
            limitOrders.stream()
                .map(order -> OkexAdapters.adaptAmendOrder(order))
                .collect(Collectors.toList()))
        .getData()
        .stream()
        .map(result -> result.getOrderId())
        .collect(Collectors.toList());
  }

  @Override
  public boolean cancelOrder(CancelOrderParams params) throws IOException {
    return "0"
        .equals(
            cancelOkexOrder(
                    OkexCancelOrderRequest.builder()
                        .orderId(((OkexTradeParams.OkexCancelOrderParams) params).getOrderId())
                        .instrumentId(
                            OkexAdapters.adaptCurrencyPairId(
                                ((OkexTradeParams.OkexCancelOrderParams) params).getCurrencyPair()))
                        .build())
                .getData()
                .get(0)
                .getCode());
  }

  public List<Boolean> cancelOrder(List<CancelOrderParams> params) throws IOException {
    return cancelOkexOrder(
            params.stream()
                .map(
                    param ->
                        OkexCancelOrderRequest.builder()
                            .orderId(((OkexTradeParams.OkexCancelOrderParams) param).getOrderId())
                            .instrumentId(
                                OkexAdapters.adaptCurrencyPairId(
                                    ((OkexTradeParams.OkexCancelOrderParams) param)
                                        .getCurrencyPair()))
                            .build())
                .collect(Collectors.toList()))
        .getData()
        .stream()
        .map(result -> "0".equals(result.getCode()))
        .collect(Collectors.toList());
  }
}
