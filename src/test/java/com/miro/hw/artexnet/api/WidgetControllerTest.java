package com.miro.hw.artexnet.api;

import com.miro.hw.artexnet.BaseTestUnit;
import com.miro.hw.artexnet.ConvertHelper;
import com.miro.hw.artexnet.DataHelper;
import com.miro.hw.artexnet.api.dto.AreaRequest;
import com.miro.hw.artexnet.api.dto.PageRequest;
import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.configuration.StorageProviderConfig;
import com.miro.hw.artexnet.domain.Widget;
import com.miro.hw.artexnet.storage.WidgetStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WidgetControllerTest extends BaseTestUnit {

    @Mock
    private StorageProviderConfig storageProviderConfig;
    @Mock
    private WidgetStorage storage;

    @InjectMocks
    private WidgetController widgetController;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        super.setUp();
        Mockito.doReturn(storage).when(storageProviderConfig).getConfiguredStorage();
        widgetController = new WidgetController(storageProviderConfig);
        mockMvc = MockMvcBuilders.standaloneSetup(widgetController).alwaysDo(print()).build();
    }

    @After
    public void tearDown() { }

    ////////////////////////////////////

    @Test
    public void createWidget_invalidData() throws Exception {
        Widget widget = DataHelper.createWidget(1, 10, 10, 20, 20);
        widget.setId(null);
        widget.setX(null);
        widget.setY(null);

        mockMvc.perform(post("/api/v1/widgets")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(ConvertHelper.objectToJsonBytes(widget)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void createWidget() throws Exception {
        Widget payload = DataHelper.createWidget(1, 10, 10, 20, 20);
        payload.setId(null);

        mockMvc.perform(post("/api/v1/widgets")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(ConvertHelper.objectToJsonBytes(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
        verify(storage, times(1)).createWidget(widgetCaptor.capture());
        verifyNoMoreInteractions(storage);

        Widget widget = widgetCaptor.getValue();
        assertEquals(1, widget.getZ_index().intValue());
        assertEquals(10, widget.getX().intValue());
        assertEquals(10, widget.getY().intValue());
        assertEquals(20, widget.getWidth());
        assertEquals(20, widget.getHeight());
    }

    @Test
    public void getWidgets_defaultPaging() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/widgets")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());

        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Area> areaCaptor = ArgumentCaptor.forClass(Area.class);
        verify(storage, times(1)).getWidgets(pageCaptor.capture(), sizeCaptor.capture(), areaCaptor.capture());
        verifyNoMoreInteractions(storage);

        assertEquals(PageRequest.DEFAULT_PAGE, pageCaptor.getValue().intValue());
        assertEquals(PageRequest.DEFAULT_ITEMS_CHUNK, sizeCaptor.getValue().intValue());
        assertNull(areaCaptor.getValue());
    }

    @Test
    public void getWidgets_specifiedPaging() throws Exception {
        final int page = 10;
        final int size = 100;
        MvcResult result = mockMvc.perform(get("/api/v1/widgets?page={page}&size={size}", page, size)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());

        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Area> areaCaptor = ArgumentCaptor.forClass(Area.class);
        verify(storage, times(1)).getWidgets(pageCaptor.capture(), sizeCaptor.capture(), areaCaptor.capture());
        verifyNoMoreInteractions(storage);

        assertEquals(page, pageCaptor.getValue().intValue());
        assertEquals(size, sizeCaptor.getValue().intValue());
        assertNull(areaCaptor.getValue());
    }

    @Test
    public void getWidgets_specifiedArea() throws Exception {
        AreaRequest areaRequest = new AreaRequest(0, 0, 100, 100);
        MvcResult result = mockMvc.perform(get("/api/v1/widgets?leftX={leftX}&leftY={leftY}&rightX={rightX}&rightY={rightY}",
                areaRequest.getLeftX(), areaRequest.getLeftY(), areaRequest.getRightX(), areaRequest.getRightY())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());

        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sizeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Area> areaCaptor = ArgumentCaptor.forClass(Area.class);
        verify(storage, times(1)).getWidgets(pageCaptor.capture(), sizeCaptor.capture(), areaCaptor.capture());
        verifyNoMoreInteractions(storage);

        assertEquals(PageRequest.DEFAULT_PAGE, pageCaptor.getValue().intValue());
        assertEquals(PageRequest.DEFAULT_ITEMS_CHUNK, sizeCaptor.getValue().intValue());

        Area area = areaCaptor.getValue();
        assertEquals(areaRequest.getLeftX().intValue(), area.getLeftBottom().getXAxis());
        assertEquals(areaRequest.getLeftY().intValue(), area.getLeftBottom().getYAxis());
        assertEquals(areaRequest.getRightX().intValue(), area.getRightTop().getXAxis());
        assertEquals(areaRequest.getRightY().intValue(), area.getRightTop().getYAxis());
    }

    @Test
    public void getWidget_id() throws Exception {
        doReturn(Optional.of(Widget.builder().build())).when(storage).getById(anyLong());
        MvcResult result = mockMvc.perform(get("/api/v1/widgets/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(storage, times(1)).getById(idCaptor.capture());
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void getWidgetsCount() throws Exception {
        doReturn(1000L).when(storage).getWidgetsCount();
        doReturn(1000L).when(storage).getWidgetsCount(any(Area.class));

        MvcResult result = mockMvc.perform(get("/api/v1/widgets/count", 1L)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());
        verify(storage, times(1)).getWidgetsCount();
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void updateWidget() throws Exception {
        Widget payload = DataHelper.createWidget(1, 10, 10, 20, 20);
        doReturn(payload).when(storage).updateWidget(any(Widget.class));
        MvcResult result = mockMvc.perform(put("/api/v1/widgets/{id}", payload.getId())
                .content(ConvertHelper.objectToJsonBytes(payload))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());

        ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
        verify(storage, times(1)).updateWidget(widgetCaptor.capture());
        verifyNoMoreInteractions(storage);
    }

    @Test
    public void removeWidget() throws Exception {
        mockMvc.perform(delete("/api/v1/widgets/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
    }

}
